(ns glam.models.user
  (:require
    [clojure.set :refer [rename-keys]]
    [clojure.spec.alpha :as s]
    [cryptohash-clj.impl.argon2 :refer [chash verify]]
    [com.fulcrologic.guardrails.core :refer [>defn => | ?]]
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]
    [dv.fulcro-util :as fu]
    [glam.neo4j.user :as user]
    [taoensso.timbre :as log]))

(defn hash-password [password]
  (chash password))

(defn verify-password [input hashed]
  (verify input hashed))

(def keymap
  {:uuid          :user/id
   :email         :user/email
   :name          :user/name
   :password_hash :user/password})

(defn change-keys [m]
  "convert neo4j internal keywords to fulcro keywords"
  (rename-keys m keymap))

(defresolver user-resolver [{:keys [neo4j]} {:user/keys [id]}]
  {::pc/input  #{:user/id}
   ::pc/output [:user/email]}
  (user/get-props neo4j id)
  (-> neo4j
      (user/get-props {:uuid id})
      (rename-keys keymap)
      (select-keys [:user/email])))

(defn get-current-user
  "Reads username (email) from the ring session"
  [{:keys [neo4j] :ring/keys [request] :as env}]
  (when-let [session (:session request)]
    (when (:session/valid? session)
      (if-let [email (:user/name session)]
        (do (log/info "HAVE A USER: " email)
            (user/get-id-by-email neo4j {:email email}))
        (do (log/info "no user")
            nil)))))

(pc/defresolver current-user-id-resolver [env _]
  {::pc/output [:app/current-user]}
  {:app/current-user (get-current-user env)})

(pc/defresolver all-users-resolver [{:keys [neo4j]} _]
  {::pc/output [{:all-users [:user/id]}]}
  {:all-users (-> neo4j
                  user/get-all
                  (rename-keys keymap)
                  (select-keys [:user/id]))})

(def resolvers [user-resolver current-user-id-resolver all-users-resolver])
