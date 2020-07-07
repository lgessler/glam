(ns glam.client.ui.project.project-detail
  (:require [com.fulcrologic.fulcro.components :as c :refer [defsc]]
            [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
            [com.fulcrologic.fulcro.data-fetch :as df]
            [com.fulcrologic.fulcro.application :as app]
            [com.fulcrologic.fulcro.ui-state-machines :as sm]
            [glam.client.router :as r]
            [glam.client.ui.auth :as auth :refer [Session]]
            [taoensso.timbre :as log]))


(defsc ProjectDetail
  [this {:project/keys [id name slug] :as props}]
  {:query         [:project/id :project/name :project/slug
                   {[:component/id :session] (c/get-query Session)}]
   :ident         :project/id
   :initial-state {}
   :route-segment (r/route-segment :project)
   :will-enter    (fn [app {:keys [id] :as route-params}]
                    (log/info "Entering: " (pr-str route-params))
                    (when id
                      (dr/route-deferred
                        [:project/id id]
                        #(df/load! app [:project/id id] ProjectDetail
                                   {:post-mutation        `dr/target-ready
                                    :post-mutation-params {:target [:project/id id]}}))))}
  [:div
   [:h4 "Projectzzz"]
   [:div (pr-str props)]
   [:div (pr-str (auth/get-session props))]
   [:div "props" (pr-str props)]
   [:div "id: " (pr-str id)]
   [:div "name: " (pr-str name)]
   [:div "slug: " (pr-str slug)]
   (r/link :projects "Projects")])

(def ui-project-detail (c/factory ProjectDetail))

