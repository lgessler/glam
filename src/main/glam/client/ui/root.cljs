(ns glam.client.ui.root
  (:require
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.components :as c :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom :refer [div]]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro.ui-state-machines :as sm]
    [glam.client.ui.task-item :refer [ui-task-list TaskList TaskForm ui-task-form]]
    [glam.client.application :refer [SPA]]
    [glam.client.router :as r]
    [glam.client.ui.task-page :refer [TaskPage]]
    [glam.auth.login :refer [ui-login Login Session session-join valid-session?]]
    [glam.auth.signup :refer [Signup]]))

 (dr/defrouter TopRouter
   [this {:keys [current-state route-factory route-props]}]
   {:router-targets [TaskPage Signup]})

 (def ui-top-router (c/factory TopRouter))

 (defn menu [{:keys [session? login]}]
   (div :.ui.secondary.pointing.menu
     (conj
       (mapv r/link (if session? [:root :tasks] [:root]))
       (ui-login login))))

  (defsc PageContainer [this {:root/keys [router login] :as props}]
    {:query         [{:root/router (c/get-query TopRouter)}
                     [::sm/asm-id ::TopRouter]
                     session-join
                     {:root/login (c/get-query Login)}]
     :ident         (fn [] [:component/id :page-container])
     :initial-state (fn [_] {:root/router             (c/get-initial-state TopRouter {})
                             :root/login              (c/get-initial-state Login {})
                             :root/signup             (c/get-initial-state Signup {})
                             [:component/id :session] (c/get-initial-state Session {})})}
    (let [current-tab (r/current-route this)
          session? (valid-session? props)]
      [:.ui.container
       ^:inline (menu {:session? session? :login login})
       ^:inline (ui-top-router router)]))

(def ui-page-container (c/factory PageContainer))

(defsc Root [_ {:root/keys [page-container]}]
  {:query         [{:root/page-container (c/get-query PageContainer)}]
   :initial-state (fn [_] {:root/page-container (c/get-initial-state PageContainer {})})}
  ^:inline (ui-page-container page-container))
