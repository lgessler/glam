(ns glam.neo4j.core
  (:require [clojure.java.io :as io]
            [neo4j-clj.core :as db]))

(defn one
  "Assert that a db result like ({:user {:age 1}}) is a seq of length 1
  and contains a map with exactly one key, and return the map, {:age 1}"
  [db-result]
  (assert (and (= 1 (count db-result))
               (map? (first db-result))
               (= 1 (count (keys (first db-result))))))
  (-> db-result first vals first))

(defn ones
  "Assert that a db result sequence like ({:user {:age 1}} {:user {:age 2}})
  is a seq of maps with exactly one key and return the same seq with only the
  values remaining, e.g. ({:age 1} {:age 2})"
  [db-result-seq]
  (assert (and (every? map? db-result-seq)
               (every? #(= 1 (count (keys %))) db-result-seq)))
  (map #(-> % vals first) db-result-seq))

(defn- apply-migration
  [db-connection resource-path]
  (db/with-transaction db-connection tx
    (doseq [query (-> resource-path clojure.java.io/resource slurp (clojure.string/split #";"))]
      (when-not (clojure.string/blank? query)
        (db/execute tx query)))))

(def migrations
  ["migrations/20200625-init.up.cypher"])

(defn init-db
  [db-connection]
  (doseq [migration migrations]
    (apply-migration db-connection migration)))

