(ns glam.xtdb.vocab-item
  (:require [xtdb.api :as xt]
            [glam.xtdb.easy :as gxe]
            [glam.xtdb.common :as gxc])
  (:refer-clojure :exclude [get merge]))

(def attr-keys [:vocab-item/id
                :vocab-item/layer
                :vocab-item/form
                :vocab-item/properties ;; arbitrary KVP map
                :config])

(defn xt->pathom [doc]
  (when doc
    (-> doc)))

(defn create [node {:vocab-item/keys [id] :as attrs}]
  (let [{:vocab-item/keys [id] :as record} (clojure.core/merge (gxc/new-record "vocab-item" id)
                                                                   (select-keys attrs attr-keys))
        tx-status (gxe/submit! node [[:xtdb.api/put record]])]
    (print "Id: " id)
    {:success tx-status
     :id      id}))

;; Queries ------------------------------------------------------------------------
(defn get
  [node id]
  (xt->pathom (gxe/find-entity node {:vocab-item/id id})))

;; Mutations ----------------------------------------------------------------------
(defn merge
  "Note: don't include the join"
  [node eid m]
  (gxe/merge node eid (select-keys m [:vocab-item/name])))

(gxe/deftx delete [node eid] 
  (let [vocab-item-deletions (gxe/delete* eid)

        vocab-map-ids (map first (xt/q (xt/db node) '{:find  [?vm]
                                                     :where [[?vm :vocab-map/vocab ?v]]
                                                     :in    [?v]}
                                      eid))
        vocab-map-deletions (mapv gxe/delete* vocab-map-ids)]
    (reduce into [vocab-item-deletions
                  vocab-map-deletions])))