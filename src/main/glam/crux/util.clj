(ns glam.crux.util
  (:require [glam.crux.easy :as gce])
  (:import (java.util UUID)))

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
  "Create a blank record with a UUID in :crux.db/id. If ns is provided,
  will also assoc this id with (keyword ns \"id\"), e.g. :person/id.
  If ns and eid are both provided, will use the eid for both keys instead
  of a random UUID. If eid is passed but is nil, will fall back to a UUID."
  ([] (new-record nil))
  ([ns]
   (let [eid (UUID/randomUUID)]
     (new-record ns eid)))
  ([ns eid]
   (cond
     (nil? eid) (new-record ns)
     (nil? ns) {:crux.db/id eid}
     :else {:crux.db/id       eid
            (keyword ns "id") eid})))

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
  "Joins from e1 to e2 at all keys specified in `join-keys`. This function is idemponent:
  if an e1->e2 join already exists at some join key on e1, nothing will change.
  This function also includes match clauses for both entities, guarding against race
  conditions."
  [node e1-id join-keys e2-id]
  (let [e1 (gce/entity node e1-id)
        e2 (gce/entity node e2-id)]
    [(gce/match* e1-id e1)
     (gce/match* e2-id e2)
     (gce/put* (reduce (fn [project join-key]
                         (-> project
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
  idemponent: if an e1->e2 join does not exist at some join key on e1, nothing will change.
  This function also includes match clauses for both entities, guarding against race
  conditions."
  [node e1-id join-keys e2-id]
  (let [e1 (gce/entity node e1-id)
        e2 (gce/entity node e2-id)]
    (into [(gce/match* e1-id e1)
           (gce/match* e2-id e2)
           (gce/put* (reduce (fn [project join-key]
                               (remove-id project join-key e2-id))
                             e1
                             join-keys))])))

(defn remove-join**
  "See `remove-from-multi-joins**`"
  [node e1-id join-key e2-id]
  (remove-from-multi-joins** node e1-id [join-key] e2-id))