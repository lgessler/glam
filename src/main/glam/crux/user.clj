(ns glam.crux.user
  (:require [crux.api :as crux]
            [glam.crux.util :as cutil]
            [glam.crux.easy :as gce])
  (:refer-clojure :exclude [get merge]))


(defn crux->pathom [doc]
  (when doc
    doc))

(defn get [node eid]
  (-> (gce/entity node eid)
      crux->pathom))

(defn get-all [node] (map crux->pathom (gce/find-entities node {:user/id '_})))
(defn get-by-name [node name] (crux->pathom (gce/find-entity node {:user/name name})))
(defn get-by-email [node email] (crux->pathom (gce/find-entity node {:user/email email})))

(defn create [node {:user/keys [name email admin? password-hash]}]
  (let [;; make the first user to sign up an admin
        first-signup? (= 0 (count (get-all node)))
        {:user/keys [id] :as record}
        (clojure.core/merge (cutil/new-record "user")
                            {:user/name          name
                             :user/email         email
                             :user/password-hash password-hash
                             :user/admin?        (if first-signup? true (boolean admin?))
                             :user/reader        #{}
                             :user/writer        #{}})]
    (gce/put node record)
    id))

(defn merge [node eid m]
  (gce/merge node eid (select-keys m [:user/name :user/email :user/password-hash :user/admin? :user/reader :user/writer])))

(defn delete* [eid] (gce/delete* eid))
(defn delete [node eid] (gce/submit! node [(delete* eid)]))

