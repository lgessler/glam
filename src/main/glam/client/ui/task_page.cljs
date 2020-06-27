(ns glam.client.ui.task-page
  (:require
    [com.fulcrologic.fulcro.components :as c :refer [defsc]]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [glam.client.ui.task-item :refer [ui-task-list TaskList TaskForm ui-task-form TaskItem]]
    [glam.client.router :as r]))

(defsc TaskPage [this {:keys [task-list task-form]}]
  {:query         [{:task-list (c/get-query TaskList)}
                   {:task-form (c/get-query TaskForm)}]
   :route-segment (r/route-segment :tasks)
   :initial-state     (fn [_] {:task-form (c/get-initial-state TaskForm)
                               :task-list (c/get-initial-state TaskList)})
   :componentDidMount (fn [this] (df/load! this :all-tasks TaskItem))
   :ident         (fn [_] [:component/id :root])}
  [:div
   [:h1 "Here's a task form:"]
   (ui-task-form task-form)
   [:h1 "Here's a task list:"]
   (ui-task-list task-list)])
