(ns glam.auth.login
  (:require
    ["react" :as react]
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom]
    [com.fulcrologic.fulcro.dom.events :as evt]
    [com.fulcrologic.fulcro.dom.html-entities :as ent]
    [com.fulcrologic.fulcro.mutations :as m]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro.ui-state-machines :as sm :refer [defstatemachine]]
    [goog.object :as g]
    [goog.events :as events :refer [EventType]]
    [sablono.core :as html :refer-macros [html]]
    [dv.fulcro-util :as fu]
    [dv.cljs-emotion :refer [defstyled]]
    [glam.auth.session :as session]
    [glam.client.router :as r]
    [taoensso.timbre :as log]))

(defsc Session
  "Session representation. Used primarily for server queries.
  On-screen representation happens in Login component."
  [_ {:keys [:session/valid? :user/name :session/server-error-msg]}]
  {:query         [:session/valid? :session/server-error-msg :user/name :ui/loading?]
   :ident         (fn [] [:component/id :session])
   :pre-merge     (fn [{:keys [data-tree]}]
                    (merge
                      {:session/valid?           false :user/name ""
                       :session/server-error-msg nil} data-tree))
   :initial-state {:session/valid? false :user/name "" :session/server-error-msg nil}})

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
           :classes  [(when loading? "loading")]} "Login"))
      (dom/div :.ui.message
        (dom/p "Don't have an account?")
        (dom/a {:onClick (fn []
                           (sm/trigger! this ::session/session :event/toggle-modal {})
                           (r/change-route! this :signup))}
          "Please sign up!")))))

(def session-join {[:component/id :session] (comp/get-query Session)})

(defn get-session [props] (get props [:component/id :session]))
(defn valid-session? [props] (:session/valid? (get props [:component/id :session])))

(defn toggle-modal! [this] (sm/trigger! this ::session/session :event/toggle-modal))
(defn close-modal! [this] (sm/trigger! this ::session/session :event/close-modal))


(defsc Login [this {:user/keys [email]
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
  (let [current-state   (sm/get-active-state this ::session/session)
        session         (get-session props)
        {current-user :user/name session-valid? :session/valid?} session
        on-start-state? (= :initial current-state)
        loading?        (= :state/checking-session current-state)
        logged-in?      session-valid?
        password        (or (comp/get-state this :password) "") ; c.l. state for security
        form-valid?     (and (fu/valid-email? email) (fu/valid-password? password))]
    [:.right.menu {:ref (fn [r] (g/set this "el" r))}

     (when-not on-start-state?
       (if logged-in?
         [:button.item
          {:on-click #(sm/trigger! this ::session/session :event/logout)}
          (fu/hover-hand nil (str current-user ent/nbsp "Log out"))]

         [html/fragment
          ^:inline (r/link :signup
                     {:onClick
                      (fn []
                        (when open? (close-modal! this))
                        (r/change-route! this :signup))})

          [:div {:className "item" :key "login" :onClick #(toggle-modal! this)}
           (fu/hover-hand #js{:key "login-label"} "Login")
           (when open?
             (ui-floating-menu
               {:className "four wide ui raised teal segment"
                :key       "floating-menu"
                ;; Stop bubbling (would trigger the menu toggle)
                :onClick   evt/stop-propagation!}
               (ui-login-form this {:error       error
                                    :email       email :password password
                                    :form-valid? form-valid? :loading? loading?})))]]))]))

(def ui-login (comp/factory Login {:keyfn (constantly "login-menu")}))
