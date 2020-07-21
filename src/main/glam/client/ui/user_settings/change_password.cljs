(ns glam.client.ui.user-settings.change-password
  (:require [com.fulcrologic.fulcro.components :as c :refer [defsc]]
            [com.fulcrologic.fulcro.algorithms.form-state :as fs]
            [com.fulcrologic.fulcro.mutations :as m]
            [com.fulcrologic.fulcro.dom :as dom]
            [sablono.core :as html :refer [html]]
            [glam.client.ui.common :as common]
            [glam.client.router :as r]
            [glam.models.session :as sn]
            [glam.models.user :as user]
            [glam.models.user-common :refer [valid-password]]))

(def ident [:component/id :change-password-form])

(defn form-valid [form field]
  (let [v (get form field)]
    (case field
      :current-password (valid-password v)
      :new-password (valid-password v)
      :new-password-confirm (= v (:new-password form)))))

(def validator (fs/make-validator form-valid))

(defn complete-field
  [this field]
  (c/transact! this [(fs/mark-complete! {:entity-ident ident
                                         :field        field})]))

(defsc ChangePasswordForm [this {:keys [current-password
                                        new-password-confirm
                                        new-password
                                        busy?]
                                 :as   props}]
  {:ident         (fn [_] ident)
   :query         [fs/form-config-join
                   :current-password
                   :new-password
                   :new-password-confirm
                   :busy?]
   :initial-state (fn [_]
                    (js/console.log "OoOoOo")
                    (fs/add-form-config ChangePasswordForm
                                        {:current-password     ""
                                         :new-password         ""
                                         :new-password-confirm ""
                                         :busy?                false}))
   :form-fields   #{:current-password :new-password :new-password-confirm}}
  (let [error? (some #(= :invalid (validator props %))
                     [:current-password :new-password :new-password-confirm])]
    ;; wrap in (html ...) so we can use Sablono's fragment tag, :*
    (html
      [:div.ui.segment
       [:h2 "Change Password"]
       [:div.ui.form {:class [(when error? "error")]}
        (common/input-with-label this :current-password "Current Password" validator "Password must be 8 or more characters long"
          {:type     "password"
           :value    current-password
           :disabled busy?
           :onBlur   #(complete-field this :current-password)
           :onChange #(m/set-string! this :current-password :event %)})
        (common/input-with-label this :new-password "New Password" validator "Password must be 8 or more characters long"
          {:type     "password"
           :value    new-password
           :disabled busy?
           :onBlur   #(complete-field this :new-password)
           :onChange #(m/set-string! this :new-password :event %)})
        (common/input-with-label this :new-password-confirm "Confirm New Password" validator "Passwords must match"
          {:type     "password"
           :value    new-password-confirm
           :disabled busy?
           :onBlur   #(complete-field this :new-password-confirm)
           :onChange #(m/set-string! this :new-password-confirm :event %)})
        [:button.ui.primary.button
         {:disabled (or (not (fs/checked? props)) (not= :valid (validator props)))}
         "Change Password"]
        [:button.ui.button
         {:onClick (fn []
                     (c/transact! this [(fs/clear-complete! {})])
                     (c/transact! this [(fs/reset-form! {})]))}
         "Reset"]]])))

(def ui-change-password-form (c/factory ChangePasswordForm))

