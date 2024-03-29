(ns glam.models.session
  (:require
    [clojure.spec.alpha :as s]
    [com.fulcrologic.fulcro.server.api-middleware :as fmw]
    [com.fulcrologic.guardrails.core :refer [>defn => | ?]]
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]
    [glam.models.user :as user]
    [glam.models.common :refer [server-error]]
    [glam.xtdb.user :as cuser]
    [glam.models.auth :refer [get-session]]
    [taoensso.timbre :as log]
    [glam.xtdb.easy :as gxe]))

(defn augment-session-resp
  "Uses `mutation-response` as the actual return value for a mutation,
   but also stores the data into the session."
  ([mutation-env new-session-data]
   (augment-session-resp mutation-env new-session-data (or new-session-data {})))
  ([mutation-env new-session-data data]
   (let [existing-session (get-session mutation-env)]
     (fmw/augment-response
       data
       (fn [resp]
         (let [new-session (cond->> new-session-data (some? new-session-data) (merge existing-session))]
           (assoc resp :session new-session)))))))

(pc/defmutation signup
  [{:keys [node] :as env} {:keys [email password]}]
  {}
  (println (cuser/get-by-email node email))
  (if-let [{:user/keys [id]} (and (string? email) (cuser/get-by-email node email))]
    (augment-session-resp env {:session/valid?           false
                               :user/email               email
                               :user/id                  id
                               :user/admin?              false
                               :session/server-error-msg "Problem signing up."})
    (do (log/info "signing up user: " email)
        ;; TODO should also check for error
        (cond
          (not (and (string? email) (user/valid-email email)))
          (augment-session-resp env {:session/server-error-msg "Invalid email. User not created."})

          (not (and (string? password) (user/valid-password password)))
          (augment-session-resp env {:session/server-error-msg "Invalid password. User not created."})

          :else
          (let [id (:id (cuser/create node {:user/name          email
                                            :user/email         email
                                            :user/password-hash (user/hash-password password)}))
                admin? (:user/admin? (gxe/entity node id))]
            (augment-session-resp env {:session/valid?           true
                                       :session/server-error-msg nil
                                       :user/email               email
                                       :user/id                  id
                                       :user/admin?              admin?}))))))

;; todo use a protocol to support pluggable auth
(defmutation login [{:keys [node] :as env} {:keys [username password]}]
  {::pc/output [:session/valid? :user/email :user/id :user/admin?]}
  (do
    (log/info "Authenticating" username)
    (if-let [{:user/keys [id password-hash admin?] :as user} (and (string? username) (cuser/get-by-email node username))]
      (if (and (string? password) (user/verify-password password password-hash))
        (do
          (log/info "Successful login: " (dissoc user :user/password-hash))
          (augment-session-resp env {:session/valid?           true
                                     :session/server-error-msg nil
                                     :user/email               username
                                     :user/admin?              admin?
                                     :user/id                  id}))
        (do
          (log/error "Invalid credentials supplied for" username)
          (server-error 401 "Invalid credentials")))
      (server-error 401 "Invalid credentials"))))

(defmutation logout [env params]
  {::pc/output [:session/valid?
                :session/server-error-msg
                :user/email :user/id :user/admin?]}
  (augment-session-resp env
                        {:session/valid?           false
                         :session/server-error-msg nil
                         :user/email               ""
                         :user/id                  nil
                         :user/admin?              false}))

(defresolver current-session-resolver [env _]
  {::pc/output [{::current-session [:session/valid? :user/email :user/id :user/admin?]}]}
  (let [{:keys [user/email session/valid? user/admin? user/id] :as session} (get-session env)]
    (log/info " in current sesh resolver: " session)
    (if valid?
      (do
        (log/info email "already logged in!")
        {::current-session {:session/valid? true :user/email email :user/admin? admin? :user/id id}})
      {::current-session {:session/valid? false}})))

(def resolvers [current-session-resolver signup login logout])
