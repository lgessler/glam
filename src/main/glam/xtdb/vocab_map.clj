(ns glam.xtdb.vocab-map
  (:require [xtdb.api :as xt]
            [glam.xtdb.easy :as gxe]
            [glam.xtdb.common :as gxc])
  (:refer-clojure :exclude [get merge]))

(def attr-keys [:vocab-map/id
                :vocab-map/document
                :vocab-map/members
                :vocab-map/vocab
                :config])

(defn xt->pathom [doc]
  (when doc
    (-> doc)))

(defn create [node {:vocab-map/keys [id] :as attrs}]
  (let [{:vocab-map/keys [id] :as record} (clojure.core/merge (gxc/new-record "vocab-map" id)
                                                                   (select-keys attrs attr-keys))
        tx-status (gxe/submit! node [[:xtdb.api/put record]])]
    (print "Id: " id)
    {:success tx-status
     :id      id}))

;; Queries ------------------------------------------------------------------------
(defn get
  [node id]
  (xt->pathom (gxe/find-entity node {:vocab-map/id id})))

;; Mutations ----------------------------------------------------------------------
(defn merge
  "Note: don't include the join"
  [node eid m]
  (gxe/merge node eid (select-keys m [:vocab-map/members :vocab-map/vocab])))

(gxe/deftx delete [node eid]
  [(gxe/delete* eid)])