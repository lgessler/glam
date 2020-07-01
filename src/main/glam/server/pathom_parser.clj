(ns glam.server.pathom-parser
  (:require
    [com.wsscode.pathom.connect :as pc]
    [com.wsscode.pathom.core :as p]
    [dv.crux-node :refer [crux-node]]
    [dv.pathom :refer [build-parser]]
    [glam.server.neo4j :refer [neo4j-conn]]
    [glam.server.task-resolvers :as task]
    [glam.models.project :refer [project-resolvers]]
    [glam.auth.session :as session]
    [glam.auth.user :as user]
    [glam.server.config :refer [config]]
    [neo4j-clj.core :as neo4j]))

(def all-resolvers
  [session/resolvers
   user/resolvers
   task/resolvers
   project-resolvers])

(def parser
  (let [{:keys [trace? index-explorer? log-responses? connect-viz?]} (::config config)]
    (build-parser
      {:resolvers          all-resolvers
       :trace?             trace?
       :index-explorer?    index-explorer?
       :log-responses?     log-responses?
       :enable-pathom-viz? connect-viz?
       :env-additions      (fn [env] {:crux-node    crux-node
                                      :neo4j        (neo4j/get-session neo4j-conn)
                                      :config       config
                                      :current-user (user/get-current-user env)})})))
