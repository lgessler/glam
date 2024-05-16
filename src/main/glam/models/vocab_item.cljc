(ns glam.models.vocab-item
  (:require [clojure.set :refer [rename-keys]]
            [com.wsscode.pathom.connect :as pc]
            [com.fulcrologic.fulcro.algorithms.form-state :as fs]
            [taoensso.timbre :as log]
            #?(:clj [glam.models.auth :as ma])
            #?(:clj [glam.models.common :as mc :refer [server-message server-error]])
            #?(:clj [glam.xtdb.vocab-layer :as vl])
            #?(:clj [glam.xtdb.vocab-item :as vi])
            #?(:clj [glam.xtdb.easy :as gxe])))

(def vocab-item-keys [:vocab-item/layer :vocab-item/form :vocab-item/properties])

;; user --------------------------------------------------------------------------------
#?(:clj
   (pc/defresolver get-vocab-item [{:keys [node]} {:vocab-item/keys [id]}]
     {::pc/input     #{:vocab-item/id}
      ::pc/output    [:vocab-item/id :vocab-item/layer :vocab-item/form :vocab-item/properties :config]
      ::pc/transform (ma/readable-required :vocab-item/id)}
     (vi/get node id)))
     
;; admin --------------------------------------------------------------------------------
;;

#?(:clj
   (pc/defmutation save-vocab-item [{:keys [node]} {delta :delta [_ id] :ident :as params}]
     {::pc/transform ma/admin-required
      ::pc/output    [:server/error? :server/message]}
       (cond
         (nil? (vi/get node id))
         (server-error (str "vocab-item with id " id " does not exist."))

         :else
         (if-not (vi/merge node id (mc/apply-delta {} delta))
           (server-error (str "Failed to save vocab-item information, please refresh and try again"))
           (gxe/entity node id)))))

#?(:clj
   (pc/defmutation create-vocab-item
     [{:keys [node] :as env} {:vocab-item/keys [id layer form properties] :as vocab-item}]
     {::pc/transform (ma/writeable-required :vocab-item-layer/id :vocab-item/layer)}
     (cond
       (not (:vocab-layer/id (vl/get node layer)))
       (server-error "vocab-layer does not exist")

       :else
       (let [{:keys [success] new-id :id} (vi/create node {:vocab-item/layer  layer
                                                          :vocab-item/form  form
                                                          :vocab-item/properties properties})]
         (if-not success
           (server-error "Failed to create vocab-item, please try again")
           (merge {:tempids {id new-id}} (server-message "vocab-item created")))))))

#?(:clj
   (pc/defmutation delete-vocab-item [{:keys [node]} {[_ id] :ident :as params}]
     {::pc/transform ma/admin-required}
     (cond
       ;; ensure the vocab layer to be deleted exists
       (not (gxe/entity node id))
       (server-error (str "vocab layer not found by ID " id))
       ;; otherwise, go ahead
       :else
       (let [tx (into (vi/delete** node id))
             success (gxe/submit! node tx)]
         (if-not success
           (server-error (str "Failed to delete vocab item " id ". Please refresh and try again"))
           (server-message (str "vocab item " id " deleted")))))))

#?(:clj
   (def vocab-item-resolvers [get-vocab-item create-vocab-item save-vocab-item
                                  delete-vocab-item]))
