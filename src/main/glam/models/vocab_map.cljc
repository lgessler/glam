(ns glam.models.vocab-map
  (:require [clojure.set :refer [rename-keys]]
            [com.wsscode.pathom.connect :as pc]
            [com.fulcrologic.fulcro.algorithms.form-state :as fs]
            [taoensso.timbre :as log]
            #?(:clj [glam.models.auth :as ma])
            #?(:clj [glam.models.common :as mc :refer [server-message server-error]])
            #?(:clj [glam.xtdb.document :as doc])
            #?(:clj [glam.xtdb.vocab-map :as vm])
            #?(:clj [glam.xtdb.vocab-item :as vi])
            #?(:clj [glam.xtdb.span :as s])
            #?(:clj [glam.xtdb.relation :as r])
            #?(:clj [glam.xtdb.token :as t])
            #?(:clj [glam.xtdb.easy :as gxe])))

(def vocab-map-keys [:vocab-map/document :vocab-map/members :vocab-map/vocab])

;; user --------------------------------------------------------------------------------
#?(:clj
   (pc/defresolver get-vocab-map [{:keys [node]} {:vocab-map/keys [id]}]
     {::pc/input     #{:vocab-map/id}
      ::pc/output    [:vocab-map/id :vocab-map/document :vocab-map/members :vocab-map/vocab :config]
      ::pc/transform (ma/readable-required :vocab-map/id)}
     (vm/get node id)))
     
;; admin --------------------------------------------------------------------------------
;;

#?(:clj
   (pc/defmutation save-vocab-map [{:keys [node]} {delta :delta [_ id] :ident :as params}]
     {::pc/transform ma/admin-required
      ::pc/output    [:server/error? :server/message]}
       (cond
         (nil? (vm/get node id))
         (server-error (str "vocab-map with id " id " does not exist."))

        ;;  (some (and
        ;;     (nil? (s/get node id))
        ;;     (nil? (r/get node id))
        ;;     (nil? (t/get node id)))
        ;;     members)
        ;;  (server-error (str "one or more members do not exist"))

        ;;  (nil? (vi/get node vocab))
        ;;  (server-error (str "vocab-item with id " vocab " does not exist."))

         :else
         (if-not (vm/merge node id (mc/apply-delta {} delta))
           (server-error (str "Failed to save vocab-map information, please refresh and try again"))
           (gxe/entity node id)))))

#?(:clj
   (pc/defmutation create-vocab-map [{:keys [node]} {delta :delta [_ temp-id] :ident :as params}]
     {::pc/transform ma/admin-required
      ::pc/output    [:server/error? :server/message]}
     (let [new-vocab-map (-> {} (mc/apply-delta delta))]
      (cond

          :else
          (let [{:keys [id success]} (vm/create node new-vocab-map)]
            (if-not success
              (server-error (str "Failed to create vocab-map, please refresh and try again"))
              {:tempids {temp-id id}})))
              )))

#?(:clj
   (pc/defmutation delete-vocab-map [{:keys [node]} {[_ id] :ident :as params}]
     {::pc/transform ma/admin-required}
     (cond
       ;; ensure the vocab layer to be deleted exists
       (not (gxe/entity node id))
       (server-error (str "vocab map not found by ID " id))
       ;; otherwise, go ahead
       :else
       (let [tx (into (vm/delete** node id))
             success (gxe/submit! node tx)]
         (if-not success
           (server-error (str "Failed to delete vocab layer " id ". Please refresh and try again"))
           (server-message (str "vocab map " id " deleted")))))))

#?(:clj
   (def vocab-map-resolvers [get-vocab-map create-vocab-map save-vocab-map
                                  delete-vocab-map]))
