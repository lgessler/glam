(ns glam.auth.session
  (:require
    [clojure.spec.alpha :as s]
    [com.fulcrologic.fulcro.server.api-middleware :as fmw]
    [com.fulcrologic.guardrails.core :refer [>defn => | ?]]
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]
    [dv.fulcro-util :as fu]
    [glam.auth.user :as user]
    [taoensso.timbre :as log]))

(defn augment-session-resp
  "Uses `mutation-response` as the actual return value for a mutation,
   but also stores the data into the session."
  ([mutation-env new-session-data]
   (augment-session-resp mutation-env new-session-data (or new-session-data {})))
  ([mutation-env new-session-data data]
   (let [existing-session (some-> mutation-env :ring/request :session)]
     (log/info "response-updating-session" new-session-data)
     (fmw/augment-response
       data
       (fn [resp]
         (let [new-session (cond->> new-session-data (some? new-session-data) (merge existing-session))]
           (log/info "Setting new session to " new-session)
           (assoc resp :session new-session)))))))

(pc/defmutation signup
  [env {:keys [email password]}]
  {::pc/sym 'glam.auth.signup/signup}
  (let [id (fu/uuid)]
    (if-let [user (user/get-user-by-email email)]
      (augment-session-resp env
        {:session/valid? false :user/name nil :session/server-error-msg "Problem signing up."})
      (do (log/info "doing signup")
          (log/info "inserting user: " (log/spy id) (log/spy email) (log/spy password))
          (user/insert-user id email password)
          (augment-session-resp env {:session/valid? true :session/server-error-msg nil :user/name email})))))

;; todo use a protocol to support pluggable auth
(defmutation login [env {:keys [username password]}]
  {::pc/output [:session/valid? :user/name]}
  (do
    (log/info "Authenticating" username)
    (if-let [{hashed-pw :user/password :as user} (user/get-user-by-email username)]
      (do (log/info "User from db: " (dissoc user :user/password))
          (if (user/verify-password password hashed-pw)
            (augment-session-resp env {:session/valid? true :user/name username})
            (do
              (log/error "Invalid credentials supplied for" username)
              (fu/server-error "Invalid credentials"))))
      (fu/server-error "Invalid credentials"))))

(defmutation logout [env params]
  {::pc/output [:session/valid?]}
  (log/info "in logout")
  (augment-session-resp env
    {:session/valid? false :session/server-error-msg nil :user/name ""}))

(defn make-session [valid? username]
  {:session/valid? valid? :user/name username})

(defresolver current-session-resolver [env _]
  {::pc/output [{::current-session [:session/valid? :user/name]}]}
  (let [{:keys [user/name session/valid?] :as session} (get-in env [:ring/request :session])]
    (log/info " in current sesh resolver: " session)
    (if valid?
      (do
        (log/info name "already logged in!")
        {::current-session {:session/valid? true :user/name name}})
      {::current-session {:session/valid? false}})))

(def resolvers [current-session-resolver signup login logout])
