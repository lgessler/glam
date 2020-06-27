(ns glam.auth.user
  (:require
    [taoensso.timbre :as log]
    [com.fulcrologic.fulcro.dom :as dom]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]
    [com.fulcrologic.fulcro.data-fetch :as df]))

(defsc User [this {:user/keys [id email  ] :keys [db/created-at]}]
  {:query [:user/id :user/email
           :db/created-at
           {:user/tasks (comp/get-query TaskItem)}
           {:user/habits (comp/get-query HabitItem)}]
   :ident :user/id}
  (dom/div nil
    (dom/h2 "user")
    (dom/div :.ui.grid
      (dom/div :.ui.five.wide.column (str "id: " id))
      (dom/div :.ui.five.wide.column (str "email: " email))
      (dom/div :.ui.five.wide.column (str "created: " created-at)))))

(def ui-user (comp/factory User {:keyfn :user/id}))

(defsc UserList [this {:keys [all-users]}]
  {:query             [{:all-users (comp/get-query User)}]
   :ident             (fn [_] [:component/id :users-list])
   :componentDidMount (fn [this] (df/load! this :all-users User {:target [:component/id :users-list :all-users] :parallel true}))}
   (dom/div "Users List"
     (map ui-user all-users)))

(def ui-user-list (comp/factory UserList))

(defn user-path
  "Normalized path to a user entity or field in Fulcro state-map"
  ([id field] [:user/id id field])
  ([id] [:user/id id]))

(defn insert-user*
  "Insert a user into the correct table of the Fulcro state-map database."
  [state-map {:keys [:user/id] :as user}]
  (assoc-in state-map (user-path id) user))

(defmutation upsert-user
  [{:keys [:user/id :user/email] :as params}]
  (action [{:keys [state]}]
    (log/info "Upsert user action")
    (swap! state
      (fn [s]
        (-> s
          (insert-user* params)
          (targeting/integrate-ident* (user-path id) :append [:all-users])))))
  (ok-action [_]
    (log/info "OK action"))
  (error-action [_]
    (log/info "Error action"))
  (remote [env]
    (-> env
      (m/returning `User)
      (m/with-target (targeting/append-to [:all-accounts])))))

