(ns glam.models.user
  (:require
    [clojure.set :refer [rename-keys]]
    [clojure.spec.alpha :as s]
    [cryptohash-clj.impl.argon2 :refer [chash verify]]
    [com.fulcrologic.guardrails.core :refer [>defn => | ?]]
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]
    [taoensso.timbre :as log]
    [glam.neo4j.user :as user]
    [glam.models.common :refer [server-error server-message]]
    ))

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
  "Reads username (email) from the ring session and returns the neo4j ID"
  [{:keys [neo4j] :ring/keys [request] :as env}]
  (when-let [session (:session request)]
    (when (:session/valid? session)
      (if-let [email (:user/email session)]
        (do (log/info "HAVE A USER: " email)
            (user/get-id-by-email neo4j {:email email}))
        (do (log/info "no user")
            nil)))))

(pc/defresolver all-users-resolver [{:keys [neo4j]} _]
  {::pc/output [{:all-users [:user/id]}]}
  {:all-users (-> neo4j
                  user/get-all
                  (rename-keys keymap)
                  (select-keys [:user/id]))})

(pc/defmutation change-password
  [{:keys [neo4j] :as env} {:keys [:user/email current-password new-password]}]
  {::pc/sym 'glam.models.user/change-password}
  (let [user-id (user/get-id-by-email neo4j {:email email})]
    (if (nil? user-id)
      (server-error (str "No user found with email " email))
      (let [{:keys [password_hash]} (user/get-props neo4j {:uuid user-id})]
        (if-not (verify-password current-password password_hash)
          (server-error (str "Current password incorrect"))
          (do
            (user/set-password-hash
              neo4j
              {:uuid          user-id
               :password_hash (hash-password new-password)})
            (server-message "Password change successful")))))))

(def resolvers [user-resolver
                all-users-resolver
                change-password])
