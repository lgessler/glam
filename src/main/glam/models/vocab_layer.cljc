(ns glam.models.vocab-layer
  (:require [clojure.set :refer [rename-keys]]
            [com.wsscode.pathom.connect :as pc]
            [com.fulcrologic.fulcro.algorithms.form-state :as fs]
            [taoensso.timbre :as log]
            #?(:clj [glam.models.auth :as ma])
            #?(:clj [glam.models.common :as mc :refer [server-message server-error]])
            #?(:clj [glam.xtdb.token-layer :as tokl])
            #?(:clj [glam.xtdb.span-layer :as sl])
            #?(:clj [glam.xtdb.vocab-layer :as vl])
            #?(:clj [glam.xtdb.easy :as gxe])
            #?(:clj [glam.xtdb.span :as s])))

(def vocab-layer-keys [:vocab-layer/name :vocab-layer/layer-type])

(defn valid-name [name] (and (string? name) (<= 1 (count name) 80)))
(defn valid-layer-type [layer-type] (and (string? layer-type)
                                         (#{ "relation" "span" "token" } layer-type)))
(defn- field-valid [field v]
  (case field
    :vocab-layer/name (valid-name v)
    :vocab-layer/layer-type (valid-layer-type v)))

(defn vocab-layer-valid [form field]
  (let [v (get form field)]
    (field-valid field v)))

(defn record-valid? [record]
  (every? (fn [[k v]]
            (field-valid k v)) (log/spy record)))

(def validator (fs/make-validator vocab-layer-valid))

;; user --------------------------------------------------------------------------------
#?(:clj
   (pc/defresolver get-vocab-layer [{:keys [node]} {:vocab-layer/keys [id]}]
     {::pc/input     #{:vocab-layer/id}
      ::pc/output    [:vocab-layer/id :vocab-layer/name :vocab-layer/layer-type :config]
      ::pc/transform (ma/readable-required :vocab-layer/id)}
     (vl/get node id)))
     
;; admin --------------------------------------------------------------------------------
;;
#?(:clj
   (pc/defmutation create-vocab-layer [{:keys [node]} {delta :delta [_ temp-id] :ident :as params}]
     {::pc/transform ma/admin-required
      ::pc/output    [:server/error? :server/message]}
     (let [new-vocab-layer (-> {} (mc/apply-delta delta))
           valid? (mc/validate-delta record-valid? delta)]
      (cond
          (not valid?)
          (server-error 400 (str "Layer is not valid, refusing to create: " delta))

          :else
          (let [{:keys [id success]} (vl/create node new-vocab-layer)]
            (if-not success
              (server-error (str "Failed to create vocab-layer, please refresh and try again"))
              {:tempids {temp-id id}})))
              )))

#?(:clj
   (pc/defmutation save-vocab-layer [{:keys [node]} {delta :delta [_ id] :ident :as params}]
     {::pc/transform ma/admin-required
      ::pc/output    [:server/error? :server/message]}
     (let [valid? (mc/validate-delta record-valid? delta)]
       (cond
         ;; must be valid
         (not valid?)
         (server-error (str "vocab layer delta invalid: " delta))
         :else
         (if-not (vl/merge node id (mc/apply-delta {} delta))
           (server-error (str "Failed to save vocab-layer information, please refresh and try again"))
           (gxe/entity node id))))))

#?(:clj
   (pc/defmutation delete-vocab-layer [{:keys [node]} {[_ id] :ident :as params}]
     {::pc/transform ma/admin-required}
     (cond
       ;; ensure the vocab layer to be deleted exists
       (not (gxe/entity node id))
       (server-error (str "vocab layer not found by ID " id))
       ;; otherwise, go ahead
       :else
       (let [name (:vocab-layer/name (gxe/entity node id))
             tx (into (vl/delete** node id))
             success (gxe/submit! node tx)]
         (if-not success
           (server-error (str "Failed to delete vocab layer " name ". Please refresh and try again"))
           (server-message (str "vocab layer " name " deleted")))))))

#?(:clj
   (def vocab-layer-resolvers [get-vocab-layer create-vocab-layer save-vocab-layer
                                  delete-vocab-layer]))
