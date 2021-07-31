(ns glam.client.ui.project.projects-page
  (:require [com.fulcrologic.fulcro.components :as c :refer [defsc]]
            [com.fulcrologic.fulcro.data-fetch :as df]
            [glam.client.router :as r]
            [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
            [com.fulcrologic.fulcro.dom :as dom]
            [glam.client.ui.material-ui :as mui]
            [taoensso.timbre :as log]))


(defsc ProjectListItem
  [this {:project/keys [id name] :as props}]
  {:query [:project/id :project/name]
   :ident :project/id}
  (mui/list-item {:button  true
                  :onClick #(r/route-to! :project {:id id})}
    (mui/list-item-text {:primary name :secondary id})))

(def ui-project-list-item (c/factory ProjectListItem {:keyfn :project/id}))

(defsc ProjectsPage [this {:keys [accessible-projects]}]
  {:query         [{:accessible-projects (c/get-query ProjectListItem)}]
   :ident         (fn [_] [:component/id :projects-page])
   :initial-state {:accessible-projects []}
   :route-segment (r/last-route-segment :projects)
   :will-enter    (fn [app route-params]
                    (dr/route-deferred
                      [:component/id :projects-page]
                      #(df/load! app :accessible-projects ProjectListItem
                                 {:target               [:component/id :projects-page :accessible-projects]
                                  :post-mutation        `dr/target-ready
                                  :post-mutation-params {:target [:component/id :projects-page]}})))}
  (log/info accessible-projects)
  (mui/box {:mx "auto" :width 400}
    (if-not (empty? accessible-projects)
      (mui/padded-paper
        (mui/list {:subheader (mui/list-subheader {:color "primary"} "Projects")}
          (mapv ui-project-list-item accessible-projects)))
      (mui/box {:my 4}
        (mui/typography
          {:variant   "subtitle1"
           :component "h3"}
          "No projects available. Contact your administrator to request access.")))))

(def ui-projects-page (c/factory ProjectsPage))
