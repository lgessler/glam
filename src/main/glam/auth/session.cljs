(ns glam.auth.session
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro.ui-state-machines :as sm]
    [com.fulcrologic.guardrails.core :refer [>defn => | ?]]
    [dv.fulcro-util :as fu]
    [glam.client.application :refer [SPA]]
    [glam.client.router :as r]
    [taoensso.timbre :as log]))

(defn clear [env]
  (sm/assoc-aliased env :error ""))

(defn logout [env]
  (log/info "logout env: " env)
  (let [env
        (-> env
            (clear)
            (sm/assoc-aliased :username "" :session-valid? false :current-user "")
            (sm/trigger-remote-mutation :actor/login-form `logout {})
            (sm/activate :state/logged-out))]
    (r/route-to! :signup)
    env))

(defn login [{::sm/keys [event-data] :as env}]
  (-> env
      (clear)
      (sm/trigger-remote-mutation :actor/login-form 'glam.auth.session/login
                                  {:username        (:username event-data)
                                   :password        (:password event-data)
                                   ::m/returning    (sm/actor-class env :actor/current-session)
                                   ::sm/ok-event    :event/complete
                                   ::sm/error-event :event/failed})
      (sm/activate :state/checking-session)))

(defn process-session-result
  "Called on app boot and to validate logging in. See if we have a session from the backend."
  [env error-message chroute?]
  (let [success? (sm/alias-value env :session-valid?)]
    (log/info "PROCESS SESSION RESULT , CHROUTE? " chroute?)
    (cond
      (and chroute? success?)
      (r/route-to! :signup)
      (not success?)
      (r/route-to! :signup))
    (cond-> (clear env)
            success? (->
                       (sm/assoc-aliased :modal-open? false)
                       (sm/activate :state/logged-in))
            (not success?) (->
                             (sm/assoc-aliased :error error-message)
                             (sm/activate :state/logged-out)))))

(defn initial-load [env]
  (sm/load env
           ::current-session
           :actor/current-session

           {::sm/ok-event    :event/complete
            ::sm/error-event :event/failed}))

(def global-events
  {:event/close-modal  {::sm/handler (fn [env] (sm/assoc-aliased env :modal-open? false))}
   :event/toggle-modal {::sm/handler (fn [env] (sm/update-aliased env :modal-open? not))}})

;; todo adapt this to load all app start data - including session
(sm/defstatemachine session-machine
  {::sm/actors
   #{:actor/login-form :actor/current-session}

   ::sm/aliases
   {:username       [:actor/login-form :user/email]
    :error          [:actor/login-form :ui/error]
    :modal-open?    [:actor/login-form :ui/open?]
    :session-valid? [:actor/current-session :session/valid?]
    :current-user   [:actor/current-session :user/name]}

   ::sm/states
   {:initial
    {::sm/target-states #{:state/logged-in :state/logged-out}
     ::sm/events        {::sm/started    {::sm/handler #(-> % (sm/assoc-aliased :error "") initial-load)}
                         :event/failed   {::sm/target-state :state/logged-out}

                         :event/complete {::sm/target-states #{:state/logged-in :state/logged-out}
                                          ;; handles the first session request on app boot
                                          ::sm/handler       #(process-session-result % "" false)}}}

    :state/checking-session
    {::sm/events (merge global-events
                        {:event/failed   {::sm/target-states #{:state/logged-out}
                                          ::sm/handler       (fn [env]
                                                               (-> env
                                                                   (clear)
                                                                   (sm/activate :state/logged-out)
                                                                   (sm/assoc-aliased :error (fu/get-server-mutation-err env))))}
                         :event/complete {::sm/target-states #{:state/logged-out :state/logged-in}
                                          ::sm/handler       #(process-session-result % "Invalid Credentials." true)}})}

    :state/logged-in
    {::sm/events (merge global-events
                        {:event/logout {::sm/target-states #{:state/logged-out}
                                        ::sm/handler       logout}})}

    :state/logged-out
    {::sm/events (merge global-events
                        {:event/signup-success {::sm/target-state :state/logged-in}
                         :event/login          {::sm/target-states #{:state/checking-session}
                                                ::sm/handler       login}})}}})

