(ns glam.xtdb.vocab-layer
  (:require [xtdb.api :as xt]
            [glam.xtdb.easy :as gxe]
            [glam.xtdb.common :as gxc])
  (:refer-clojure :exclude [get merge]))

(def attr-keys [:vocab-layer/id
                :vocab-layer/name
                :vocab-layer/layer-type ;; e.g. token/span/relation
                :config])

(defn xt->pathom [doc]
  (when doc
    (-> doc)))

(defn create [node {:vocab-layer/keys [id] :as attrs}]
  (let [{:vocab-layer/keys [id] :as record} (clojure.core/merge (gxc/new-record "vocab-layer" id)
                                                                   (select-keys attrs attr-keys))
        tx-status (gxe/submit! node [[:xtdb.api/put record]])]
    (print "Id: " id)
    {:success tx-status
     :id      id}))

;; Queries ------------------------------------------------------------------------
(defn get
  [node id]
  (xt->pathom (gxe/find-entity node {:vocab-layer/id id})))

;; Mutations ----------------------------------------------------------------------
(defn merge
  "Note: don't include the join"
  [node eid m]
  (gxe/merge node eid (select-keys m [:vocab-layer/name :vocab-layer/layer-type])))

(gxe/deftx delete [node eid] 
  (let [vocab-layer-deletion (gxe/delete* eid)

        vocab-item-ids (map first (xt/q (xt/db node) '{:find  [?vi]
                                                     :where [[?vi :vocab-item/layer ?vl]]
                                                     :in    [?vl]}
                                      eid))
        vocab-item-deletions (mapv gxe/delete* vocab-item-ids)]
    (reduce into [vocab-item-deletions
                  vocab-layer-deletion])))