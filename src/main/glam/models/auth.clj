(ns glam.models.auth
  (:require [taoensso.timbre :as log]
            [glam.models.common :as mc]
            [com.wsscode.pathom.connect :as pc]
            [glam.xtdb.easy :as gxe]
            [glam.xtdb.access :as access]))

(defn get-session [m]
  (or (get-in m [:ring/request :session])
      (get m :session)))

;; pathom security transforms
(defn make-auth-transform [auth-fn failure-message]
  "Make a transform for a Pathom resolver that checks whether the user has sufficient
  permissions for a given operation. auth-fn is a 2-arg function env -> params -> boolean"
  (fn auth-transform [{::pc/keys [mutate resolve] :as outer-env}]
    (let [pathom-action (if mutate mutate resolve)
          pathom-action-kwd (if mutate ::pc/mutate ::pc/resolve)]
      (-> outer-env
          ;; apply the transformation
          (assoc
            pathom-action-kwd
            (fn [env params]
              ;;(log/info (str "Hit resolver/mutator: " (pr-str (::pc/resolver-data env))))
              (if (auth-fn env params)
                (pathom-action env params)
                (let [msg (str "Unauthorized pathom action: session " (get-session env)
                               " does not satisfy authorization requirement: " failure-message)]
                  (log/warn (str "Unauthorized request: " msg))
                  (mc/server-error 403 msg)))))))))

;; TODO: this auth pattern might be unperformant--one idea: cache the auth check in the pathom environment
;; see https://blog.wsscode.com/pathom/#updating-env

(defn- readable-required-fn [id-key param-key {:keys [node] :as env} params]
  (when-not (param-key params)
    (throw (ex-info "Tried to determine if id-key was writeable for a user, but it was not present in params."
                    {:id-key       id-key
                     :param-key    param-key
                     :params       params
                     :resolver-env env})))
  (let [user-id (:user/id (get-session env))
        id (param-key params)]
    (access/ident-readable? node user-id [id-key id])))

(defn- writeable-required-fn [id-key param-key {:keys [node] :as env} params]
  (when-not (param-key params)
    (throw (ex-info "Tried to determine if id-key was writeable for a user, but it was not present in params."
                    {:id-key       id-key
                     :param-key    param-key
                     :params       params
                     :resolver-env env})))
  (let [user-id (:user/id (get-session env))
        id (param-key params)]
    (access/ident-writeable? node user-id [id-key id])))

;; id-key is something like :token/id, which specifies the kind of document
;; we need to check permissions for. param-key is the key under which we will
;; find the actual value of this ID in the parameters when we're doing a real
;; auth check. These are typically the same, but we provide the arity 2 version
;; of these functions in case they're not.
(defn readable-required
  ([id-key]
   (readable-required id-key id-key))
  ([id-key param-key]
   (make-auth-transform
     (partial readable-required-fn id-key param-key)
     "current user cannot read the project involved in this query")))
(defn writeable-required
  ([id-key]
   (writeable-required id-key id-key))
  ([id-key param-key]
   (make-auth-transform
     (partial writeable-required-fn id-key param-key)
     "current user cannot modify the project involved in this query")))

(defn- level-authorized
  "Given a resolver's environment, say whether it is authorized for a given level"
  [level env params]
  (let [{:keys [session/valid? user/admin?]} (get-session env)]
    (or (nil? level)
        (and (= level :admin) admin?)
        (and (= level :user) valid?))))
(def admin-required (make-auth-transform (partial level-authorized :admin) "admin required"))
(def user-required (make-auth-transform (partial level-authorized :user) "valid login required"))

(defn ident-locked? [env ident]
  (let [user-id (:user/id (get-session env))]
    (access/ident-locked? (:node env) user-id ident)))

(defn lock-holder-name [{:keys [node]} ident]
  (let [user-id (access/ident->lock-holder node ident)]
    (if (nil? user-id)
      nil
      (let [{:user/keys [name email]} (gxe/entity node user-id)]
        (str name " [" email "]")))))

(defn lock-holder-error-msg [env ident]
  (if-let [name (lock-holder-name env ident)]
    (str "Lock required for this operation but is held by " name)
    "Lock required for this operation. Acquire it before proceeding."))
