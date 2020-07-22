(ns glam.models.user
  (:require [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
            [taoensso.timbre :as log]
            [com.fulcrologic.fulcro.components :as c]))

(defmutation change-password
  "Changes the user's password given a :user/email, :current-password, and :new-password.
  on-success and on-error are transaction vectors that will be transacted when a server
  response is given. message-handler will be given the server message, if there is one"
  [{:keys [on-success on-error message-handler form-component]}]
  (action [{:keys [app]}]
    (log/info "Beginning change-password"))
  (remote [{:keys [ast]}]
    (update ast :params dissoc :message-handler :form-component))
  (ok-action [{:keys [result] :as env}]
    (log/info "change password OK")
    (when-not (empty? on-success)
      (c/transact! form-component on-success))
    (if-let [message (get-in result [:body `change-password])]
      (c/transact! form-component [(message-handler message)])))
  (error-action [{:keys [result] :as env}]
    (log/info "change password error")
    (when-not (empty? on-error)
      (c/transact! form-component on-error))
    (if-let [message (get-in result [:body `change-password])]
      (c/transact! form-component [(message-handler message)]))))

