(ns glam.xtdb.common
  (:require [glam.xtdb.easy :as gxe]
            [glam.server.id-counter :as id-counter]
            [taoensso.timbre :as log]
            [clojure.pprint :refer [pprint]]
            [com.fulcrologic.fulcro.algorithms.tempid :as tempid]))

(defn document-mutation? [mutation]
  (#{"glam.models.document"
     "glam.models.text"
     "glam.models.text-layer"
     "glam.models.token"
     "glam.models.token-layer"
     "glam.models.span"
     "glam.models.span-layer"} (-> mutation first namespace)))

(defn try-get-ident [mutation]
  (let [{document-id :document/id
         text-id     :text/id
         token-id    :token/id
         span-id     :span/id} (second mutation)]
    (cond document-id [:document/id document-id]
          text-id [:text/id text-id]
          token-id [:token/id token-id]
          span-id [:span/id span-id]
          :else nil)))

(defn get-affected-doc [node [mutation-symbol params :as mutation] pathom-response]
  (let [starting-ident (try-get-ident mutation)
        starting-ident (if (tempid/tempid? (second starting-ident))
                         (if-let [resolved-id (get-in pathom-response [mutation-symbol :tempids (second starting-ident)])]
                           [(first starting-ident) resolved-id]
                           nil)
                         starting-ident)]
    (loop [[id-type id :as ident] starting-ident]
      (case id-type
        :document/id ident
        :text/id (recur [:document/id (:text/document (gxe/entity node id))])
        :token/id (recur [:text/id (:token/text (gxe/entity node id))])
        :span/id (recur [:token/id (first (:span/tokens (gxe/entity node id)))])
        nil))))


(defn identize
  "Turn a single id or seq of ids into a (Pathom-style) ident or vec of idents, respectively.
  Note that this will look like {:foo/id 1} instead of [:foo/id 1] -- the latter is a Fulcroism,
  the former is what Pathom needs."
  [id-or-id-seq id-attr]
  (if (coll? id-or-id-seq)
    (vec (for [id id-or-id-seq]
           {id-attr id}))
    {id-attr id-or-id-seq}))

;; conveniences
(defn new-record
  "Create a blank record with a UUID in :xt/id. If ns is provided,
  will also assoc this id with (keyword ns \"id\"), e.g. :person/id.
  If ns and eid are both provided, will use the eid for both keys instead
  of a random UUID. If eid is passed but is nil, will fall back to a UUID."
  ([] (new-record nil))
  ([ns]
   (let [eid (id-counter/new-id!)]
     (new-record ns eid)))
  ([ns eid]
   (cond
     (nil? eid) (new-record ns)
     (nil? ns) {:xt/id eid}
     :else {:xt/id            eid
            (keyword ns "id") eid})))

(defn create-record
  "Wrapper around new-record that guarantees records will not have extraneous keys"
  [kw-ns id attrs attr-keys]
  (merge (new-record kw-ns id)
         (select-keys attrs attr-keys)))

(defn remove-id
  "Remove an ID from a to-many join"
  [entity key target-id]
  (let [new-vec (vec (filter #(not= % target-id) (key entity)))]
    (assoc entity key new-vec)))

(defn conj-unique
  "Like conj, but only fires if x is not present"
  [coll x]
  (if (some (hash-set x) coll)
    coll
    (conj coll x)))

;; join conveniences
(defn add-to-multi-joins**
  "Joins from e1 to e2 at all keys specified in `join-keys`. This function is idempotent:
  if an e1->e2 join already exists at some join key on e1, nothing will change."
  [node e1-id join-keys e2-id]
  (let [e1 (gxe/entity node e1-id)]
    [(gxe/put* (reduce (fn [entity join-key]
                         (-> entity
                             (update join-key conj-unique e2-id)
                             ;; in case this is the first assoc, turn the list into a vector
                             (update join-key vec)))
                       e1
                       join-keys))]))

(defn add-join**
  "See `add-to-multi-joins**`"
  [node e1-id join-key e2-id]
  (add-to-multi-joins** node e1-id [join-key] e2-id))

(defn remove-from-multi-joins**
  "Remove joins from e1 to e2 at all keys specified in `join-keys`. This function is
  idempotent: if an e1->e2 join does not exist at some join key on e1, nothing will change."
  [node e1-id join-keys e2-id]
  (let [e1 (gxe/entity node e1-id)]
    [(gxe/put* (reduce (fn [entity join-key]
                         (remove-id entity join-key e2-id))
                       e1
                       join-keys))]))

(defn remove-join**
  "See `remove-from-multi-joins**`"
  [node e1-id join-key e2-id]
  (remove-from-multi-joins** node e1-id [join-key] e2-id))