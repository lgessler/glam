(ns glam.client.ui.task-item
  (:require
    [clojure.string :as str]
    [clojure.spec.alpha :as s]
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.fulcrologic.fulcro.components :as c :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.ui-state-machines :as sm]
    [com.fulcrologic.semantic-ui.collections.form.ui-form :refer [ui-form]]
    [com.fulcrologic.semantic-ui.collections.form.ui-form-button :refer [ui-form-button]]
    [com.fulcrologic.semantic-ui.collections.form.ui-form-field :refer [ui-form-field]]
    [com.fulcrologic.semantic-ui.modules.checkbox.ui-checkbox :refer [ui-checkbox]]
    [com.fulcrologic.semantic-ui.modules.dropdown.ui-dropdown :refer [ui-dropdown]]
    [com.fulcrologic.semantic-ui.modules.dropdown.ui-dropdown-item :refer [ui-dropdown-item]]
    [com.fulcrologic.semantic-ui.modules.dropdown.ui-dropdown-menu :refer [ui-dropdown-menu]]
    [com.fulcrologic.semantic-ui.modules.transition.ui-transition :refer [ui-transition]]
    [dv.fulcro-util :as fu]
    [dv.fulcro-entity-state-machine :as fmachine]
    [glam.models.task :as dm]
    [sablono.core :refer [html]]
    [taoensso.timbre :as log]))

(defsc TaskItem
  [this {:task/keys [id description] :as props}]
  {:query (fn [_] dm/all-task-keys)
   :ident :task/id}
  [:div
   [:h4 "Task Item"]
   [:div "id: " (pr-str id)]
   [:div "descr: " (pr-str description)]
   (fu/props-data-debug this true)])

(def ui-task-item (c/factory TaskItem {:keyfn :task/id}))

(defsc TaskList [this {:keys [all-tasks]}]
  {:initial-state {}
   :query         [{[:all-tasks '_] (c/get-query TaskItem)}]}
  [:div "This is the list of tasks"
   [:.ui.divider]
   (map ui-task-item all-tasks)])

(def ui-task-list (c/factory TaskList))

(defn task-item-card
  [{:task/keys [id description]}]
  [:.ui.card {:key id}
   [:.content
    [:.ui.tiny.horizontal.list
     [:.item description]]]])

(defn empty-form [] (dm/make-task {:task/description ""}))

(defn task-valid [form field]
  (let [v (get form field)]
    (s/valid? field v)))

(def validator (fs/make-validator task-valid))

(defsc TaskItemReturn [this props]
  {:query [:server/message :server/error?
           :task/id
           :task/duration
           :task/scheduled-at
           :task/description]
   :ident (fn [_] [:component/id :task-item-return])})

(defsc TaskForm
  [this {:keys [server/message ui/machine-state ui/loading? ui/show-form-debug?] :as props}
   {:keys [cb-on-submit on-cancel]}]
  {:query             [:task/id :task/description fs/form-config-join
                       :ui/machine-state :ui/loading? :server/message
                       (sm/asm-ident ::form-machine)
                       :ui/show-form-debug?]
   :ident             :task/id
   :form-fields       #{:task/description}
   :initial-state     (fn [_] (fs/add-form-config
                                TaskForm (merge (empty-form)
                                           {:ui/show-form-debug? false})))
   :componentDidMount (fn [this] (fmachine/begin! this ::form-machine TaskItemReturn))}
  (let [{:keys [checked? disabled?]} (fu/validator-state validator props)]
    [:div
     ^:inline (fu/notification {:ui/submit-state machine-state :ui/server-message message})
     #_#_
     (when goog.DEBUG
         (fu/ui-button #(m/toggle! this :ui/show-form-debug?) "Debug form"))
     (fu/form-debug validator this show-form-debug?)

     [:h3 nil "Enter a new task"]

     (ui-form #js{:className (when checked? "error") :as "div"
                  :onChange  (fn [e]
                               (sm/trigger! this ::form-machine :event/reset)
                               true)}
       (ui-form-field nil
         (fu/ui-textfield this "Task Description" :task/description props :tabIndex 1
           :autofocus? true))

       (html [:.ui.grid
              [:.column.four.wide
               [:button
                {:tabIndex 2
                 :disabled disabled?
                 :onClick  (fu/prevent-default
                             #(let [task (dm/fresh-task props)]
                                (fmachine/submit-entity! this
                                  {:entity          task
                                   :machine         ::form-machine
                                   :remote-mutation `create-task
                                   :on-reset        cb-on-submit
                                   :target          {:append [:all-tasks]}})))
                 :class    (str "ui primary button" (when loading? " loading"))}
                "Enter"]]

              (when on-cancel
                [:.column.four.wide
                 [:button :.ui.secondary.button.column
                  {:on-click on-cancel} "Cancel"]])]))]))

(def ui-task-form (c/factory TaskForm {:keyfn :task/id}))
