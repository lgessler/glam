(ns glam.server.server-entry
  (:require
    [mount.core :as mount]
    glam.server.server)
  (:gen-class))

(defn -main [& args]
  (println "args: " args)
  (mount/start-with-args {:config "config/prod.edn"}))
