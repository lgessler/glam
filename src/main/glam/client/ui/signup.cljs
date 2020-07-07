(ns glam.client.ui.signup
  (:require
    [clojure.string :as str]
    [clojure.pprint :refer [pprint]]
    [com.fulcrologic.fulcro.algorithms.denormalize :as fdn]
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.fulcro.dom :as dom]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.ui-state-machines :as uism]
    [com.fulcrologic.guardrails.core :refer [>defn => | ?]]
    [dv.fulcro-util :as fu]
    [glam.models.session :as session]
    [glam.client.router :as r]
    [glam.client.ui.auth :refer [session-join Session get-session]]
    [sablono.util :as su]
    [taoensso.timbre :as log]))

(def signup-ident [:component/id :signup])
(declare Signup)

(defn clear-signup-form*
  "Mutation helper: Updates state map with a cleared signup form that is configured for form state support."
  [state-map]
  (log/info "Clearing signup form")
  (-> state-map
    (assoc-in signup-ident
      {:account/email          ""
       :account/password       ""
       :account/password-again ""})
    (fs/add-form-config* Signup signup-ident)))

(defmutation clear-signup-form [_]
  (action [{:keys [state]}]
    (swap! state clear-signup-form*)))

(defmutation signup [_]
  (action [{:keys [state]}]
    (log/info "Starting signup mutation")
    (swap! state
      (fn [s]
        (-> s
          (fs/mark-complete* signup-ident)
          (assoc-in [df/marker-table ::signup] {:status :loading})))))

  (ok-action [{:keys [app state result]}]
    (let [state @state
          session (fdn/db->tree (comp/get-query Session) [:component/id :session] state)]
      (log/info "Signup success result: " result)
      (df/remove-load-marker! app ::signup)
      (when (:session/valid? session)
        (r/route-to! :signup)
        (uism/trigger! app ::session/session :event/signup-success))))

  (error-action [{:keys [app]}]
    (df/remove-load-marker! app ::signup))

  (remote [{:keys [state] :as env}]
    (let [{:account/keys [email password password-again]} (get-in @state signup-ident)]
      (let [valid? (boolean (and (fu/valid-email? email) (fu/valid-password? password)
                              (= password password-again)))]
        (when valid?
          (-> env (m/returning Session)))))))

(defmutation
  mark-complete!* [{field :field}]
  (action [{:keys [state]}]
    (log/info "Marking complete field: " field)
    (swap! state fs/mark-complete* signup-ident field)))

(defn mark-complete!
  [this field]
  (comp/transact!! this [(mark-complete!* {:field field})]))

(defn signup-valid [form field]
  (let [v (get form field)]
    (case field
      :account/email (fu/valid-email? v)
      :account/password (fu/valid-password? v)
      :account/password-again (= (:account/password form) v))))

(def validator (fs/make-validator signup-valid))

(defsc Signup [this {:account/keys [email password password-again] :as props}]
  {:query             [:account/email :account/password :account/password-again fs/form-config-join session-join
                       [df/marker-table ::signup]]
   :initial-state     (fn [_]
                        (fs/add-form-config Signup
                                            {:account/email          ""
                                             :account/password       ""
                                             :account/password-again ""}))
   :form-fields       #{:account/email :account/password :account/password-again}
   :ident             (fn [] signup-ident)
   :route-segment     (r/route-segment :signup)
   :componentDidMount (fn [this] (comp/transact! this [(clear-signup-form {})]))}
  (let [server-err (:session/server-error-msg (get-session props))
        form-valid? (= :valid (validator props))
        submit! (fu/prevent-default
                  #(when form-valid?
                     (comp/transact! this [(signup {:password password :email email})])))
        checked? (fs/checked? props)
        mark-complete! (partial mark-complete! this)
        saving? (df/loading? (get props [df/marker-table ::signup]))]
    [:div
     [:h3 "Signup"]
     [:form
      {:class    (str "ui form" (when checked? " error"))
       :onSubmit submit!}
      ^:inline (fu/ui-email this :account/email email mark-complete! :autofocus? true
                 :tabIndex 1)
      ^:inline (fu/ui-password2 this :account/password password :tabIndex 2)
      ^:inline (fu/ui-verify-password this :account/password-again
                 password password-again mark-complete!
                 :tabIndex 3)
      (when-not (empty? server-err) [:.ui.error.message server-err])
      [:button
       {:type      "submit"
        :tab-index 4
        :class     (str "ui primary button" (when saving? " loading"))
        :disabled  (not form-valid?)} "Sign Up"]]]))
