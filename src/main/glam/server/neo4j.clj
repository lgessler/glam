(ns glam.server.neo4j
  (:require [mount.core :refer [defstate args]]
            [glam.server.config :refer [config]]
            [neo4j-clj.core :as neo4j])
  (:import (java.net URI)
           (java.lang AutoCloseable)))

(defstate neo4j-conn
  :start (let [{:keys [url username password]} (::config config)]
           (neo4j/connect (URI. url) username password))
  :stop (neo4j/disconnect neo4j-conn))
