(ns glam.server.db-layer
  (:require
    [com.fulcrologic.guardrails.core :refer [>defn >def | => ?]]
    [glam.models.task :as dm]
    [dv.crux-util :as cu]
    [taoensso.timbre :as log]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; DB API
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-task [crux-node id] (select-keys (cu/domain-entity crux-node id) dm/db-task-keys))
(defn get-all-tasks [crux-node]
  (cu/crux-select crux-node dm/all-task-keys))
(def insert-task (partial cu/insert-entity :task/id))
(def read-merge-task (partial cu/read-merge-entity :task/id))

(def get-task-by-description (cu/mk-get-id-from-coll :task/id :task/description))
(comment (get-task-by-description [] "Drink 1 liter of water"))

(def read-merge-user (partial cu/read-merge-entity :user/id))
;; does db update
(def merge-user!
  "(merge-user! {:new user data})"
  (partial cu/merge-domain-entity :user/id))
