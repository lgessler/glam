(ns glam.neo4j.core
  (:require [clojure.java.io :as io]
            [neo4j-clj.core :as db]))

(defn squish-key
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

;; Cypher (and therefore calls to neo4j-clj's `defquery` as well)
;; returns sequences of maps by default. But sometimes we can tell
;; that for a certain query we'll always want either (1) a single value
;; instead of a sequence, (2) only the value of the map when it has a
;; single k-v pair, or (3) both. We'll extend the defquery macro to handle
;; these cases, using "1" to indicate we want a single return value, and
;; "keys" to indicate maps will have MORE than one k-v pair.
;; defquery

;; no key squishing, many results
(defmacro defquery-nosquish
  [query-name query]
  `(db/defquery ~query-name ~query))

;; no key squishing, one result
(defmacro defquery-nosquish-1
  [query-name cypher-query]
  (let [internal-query (symbol (str query-name "__internal"))]
    `(do
       (db/defquery ~internal-query ~cypher-query)
       (defn ~query-name
         ([sess#]
          (-> sess#
              (~internal-query)
              first))
         ([sess# params#]
          (-> sess#
              (~internal-query params#)
              first))))))

;; key squishing, many results
(defmacro defquery
  [query-name cypher-query]
  (let [internal-query (symbol (str query-name "__internal"))]
    `(do
       (db/defquery ~internal-query ~cypher-query)
       (defn ~query-name
         ([sess#]
          (-> sess#
              (~internal-query)
              squish-key))
         ([sess# params#]
          (-> sess#
              (~internal-query params#)
              squish-key))))))

;; key squishing, one result
(defmacro defquery-1
  [query-name cypher-query]
  (let [internal-query (symbol (str query-name "__internal"))]
    `(do
       (db/defquery ~internal-query ~cypher-query)
       (defn ~query-name
         ([sess#]
          (-> sess#
              (~internal-query)
              squish-key
              first))
         ([sess# params#]
          (-> sess#
              (~internal-query params#)
              squish-key
              first))))))

