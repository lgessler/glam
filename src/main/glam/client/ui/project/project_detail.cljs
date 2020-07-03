(ns glam.client.ui.project.project-detail
  (:require [com.fulcrologic.fulcro.components :as c :refer [defsc]]
            [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
            [com.fulcrologic.fulcro.data-fetch :as df]
            [com.fulcrologic.fulcro.application :as app]
            [glam.client.router :as r]))



(defsc ProjectDetail
  [this {:project/keys [id name slug] :as props}]
  {:query         (fn [_] [:project/id :project/name :project/slug])
   :route-segment [:project/slug]
   :ident         :project/id
   :will-enter    (fn [app {:project/keys [slug] :as route-params}]
                    ;; we know the slug, but...
                    (when slug
                      (when-let [id (->> app
                                         ::app/state-atom
                                         deref
                                         :project/id
                                         (filter (fn [id {:project/keys [other-slug]}] (= slug)))
                                         first
                                         first)]
                        (dr/route-deferred
                          [:project/id id]
                          #(df/load! app [:project/id id] ProjectDetail
                                     {:post-mutation        `dr/target-ready
                                      :post-mutation-params {:target [:project/id id]}})))))}
  [:div
   [:h4 "Project"]
   [:div "id: " (pr-str id)]
   [:div "name: " (pr-str name)]
   [:div "slug: " (pr-str slug)]
   (r/link "/project/" "Projects")
   ])
