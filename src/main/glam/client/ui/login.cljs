(ns glam.client.ui.login
  (:require [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.ui-state-machines :as sm]
            [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.dom.events :as evt]
            [goog.object :as g]
            [goog.events :as events :refer [EventType]]
            [taoensso.timbre :as log]
            [glam.models.session :refer [session-join get-session]]
            [glam.client.router :as r]
            [glam.models.session :as session]
            [dv.cljs-emotion :refer [defstyled]]
            [dv.fulcro-util :as fu]
            [com.fulcrologic.fulcro.dom.html-entities :as ent]
            [sablono.core :as html]))

(defn toggle-modal! [this] (sm/trigger! this ::session/session :event/toggle-modal))
(defn close-modal! [this] (sm/trigger! this ::session/session :event/close-modal))

(defstyled ui-floating-menu :div
  {:position "absolute !important"
   :z-index  1000
   :width    "300px"
   :right    "0px"
   :top      "50px"})

(defn ui-login-form
  [this {:keys [loading? error email password form-valid?]}]
  (dom/div nil
           (dom/h3 :.ui.header "Login")
           (dom/form :.ui.form
                     {:classes  [(when (seq error) "error")]
                      :onSubmit (fu/prevent-default
                                  #(when form-valid?
                                     (sm/trigger! this ::session/session :event/login {:username email :password password})))}
                     (fu/ui-email this :user/email email identity :autofocus? true)
                     (fu/ui-password-field
                       password
                       #(comp/set-state! this {:password (evt/target-value %)}))

                     (dom/div :.ui.error.message error)
                     (dom/div :.ui.field
                              (dom/button :.ui.button
                                          {:type     "submit"
                                           :disabled (not form-valid?)
                                           :classes  [(when loading? "loading")]} "Login")))))

(defsc NavbarLogin [this {:user/keys [email]
                          :ui/keys   [error open?] :as props}]
  {:query              [:ui/open? :ui/error :user/email
                        session-join
                        [::sm/asm-id :glam.client.ui.root/TopRouter]
                        [::sm/asm-id ::session/session]]
   :initial-state      {:user/email "" :ui/error ""}
   :ident              (fn [] [:component/id :login])
   :componentDidMount  (fn [this]
                         (events/listen js/document (.-MOUSEUP EventType)
                                        (fn [e]
                                          ;(log/info "GOT MOUSE UP document" e)
                                          (when (:ui/open? (comp/props this))
                                            (log/info "closing modal")
                                            (close-modal! this))))

                         (when-let [dom-node (g/get this "el")]
                           (events/listen dom-node (.-MOUSEUP EventType) (fu/stop-propagation)
                                          ; (fn [e] (log/info "GOT MOUSE UP login " e) (.stopPropagation e))
                                          )))

   :componentDidUpdate (fn [this pprops _]
                         ;; clear password input field after logging in.
                         (let [{curr-session-valid? :session/valid?} (get (comp/props this) [:component/id :session])
                               {prev-session-valid? :session/valid?} (get pprops [:component/id :session])]
                           (when (and curr-session-valid? (not prev-session-valid?))
                             (comp/set-state! this {:password ""}))))}
  (let [current-state (sm/get-active-state this ::session/session)
        session (get-session props)
        {current-user :user/email session-valid? :session/valid?} session
        on-start-state? (= :initial current-state)
        loading? (= :state/checking-session current-state)
        logged-in? session-valid?
        password (or (comp/get-state this :password) "")    ; c.l. state for security
        form-valid? (and (fu/valid-email? email) (fu/valid-password? password))]
    [:.right.menu {:ref (fn [r] (g/set this "el" r))}

     (when-not on-start-state?
       (if logged-in?
         [html/fragment
          (r/link :user-settings "Settings")
          [:button.item
           {:on-click #(sm/trigger! this ::session/session :event/logout)}
           (fu/hover-hand nil (str current-user ent/nbsp "Log out"))]]

         #_[html/fragment
          #_(r/link :settings {} "Settings")
          [:div {:className "item" :key "login" :onClick #(toggle-modal! this)}
           (fu/hover-hand #js{:key "login-label"} "Login")
           (when open?
             (ui-floating-menu
               {:className "four wide ui raised teal segment"
                :key       "floating-menu"
                ;; Stop bubbling (would trigger the menu toggle)
                :onClick   evt/stop-propagation!}
               (ui-login-form this {:error       error
                                    :email       email
                                    :password    password
                                    :form-valid? form-valid?
                                    :loading?    loading?})))]]))]))

(def ui-navbar-login (comp/factory NavbarLogin {:keyfn (constantly "login-menu")}))

(defsc Login [this {:user/keys [email]
                    :ui/keys   [error open?] :as props}]
  {:query              [:ui/open? :ui/error :user/email
                        session-join
                        [::sm/asm-id ::session/session]]
   :initial-state      {:user/email "" :ui/error ""}
   :ident              (fn [] [:component/id :login])
   :componentDidUpdate (fn [this pprops _]
                         ;; clear password input field after logging in.
                         (let [{curr-session-valid? :session/valid?} (get (comp/props this) [:component/id :session])
                               {prev-session-valid? :session/valid?} (get pprops [:component/id :session])]
                           (when (and curr-session-valid? (not prev-session-valid?))
                             (comp/set-state! this {:password ""}))))}
  (let [current-state (sm/get-active-state this ::session/session)
        session (get-session props)
        {session-valid? :session/valid?} session
        loading? (= :state/checking-session current-state)
        password (or (comp/get-state this :password) "")    ; c.l. state for security
        form-valid? (and (fu/valid-email? email) (fu/valid-password? password))]
    (when-not session-valid?
      (ui-login-form this {:error       error
                           :email       email
                           :password    password
                           :form-valid? form-valid?
                           :loading?    loading?}))))

(def ui-login (comp/factory Login))
