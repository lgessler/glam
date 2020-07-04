(ns glam.client.ui.project.projects-page
  (:require [com.fulcrologic.fulcro.components :as c :refer [defsc]]
            [com.fulcrologic.fulcro.data-fetch :as df]
            [dv.fulcro-util :as fu]
            [glam.client.router :as r]))


(defsc ProjectListItem
  [this {:project/keys [id name slug] :as props}]
  {:query (fn [_] [:project/id :project/name :project/slug])
   :ident :project/id}
  [:div.ui.breadcrumb
   ]
  [:div
   [:h4 "Project"]
   [:div "id: " (pr-str id)]
   [:div "name: " (pr-str name)]
   [:div "slug: " (pr-str slug)
    (r/link :project {:id id} name)

    #_(r/link :project {:onClick #(r/change-route! :project)})]
   (fu/props-data-debug this true)])

(def ui-project-item (c/factory ProjectListItem {:keyfn :project/id}))

(defsc ProjectList [this {:keys [all-projects]}]
  {:initial-state {}
   :query         [{[:all-projects '_] (c/get-query ProjectListItem)}]}
  [:div "This is the list of projects"
   [:.ui.divider]
   (map ui-project-item all-projects)])

(def ui-project-list (c/factory ProjectList))

(defsc ProjectsPage [this {:keys [project-list]}]
  {:query             [{:project-list (c/get-query ProjectList)}]
   :route-segment     [""]
   :initial-state     (fn [_] {:project-list (c/get-initial-state ProjectList)})
   :componentDidMount (fn [this] (df/load! this :all-projects ProjectListItem))
   :ident             (fn [_] [:component/id :projects-page])}
  [:div
   (ui-project-list project-list)])

(def ui-projects-page (c/factory ProjectsPage))
