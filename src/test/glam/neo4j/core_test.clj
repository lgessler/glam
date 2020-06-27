(ns glam.neo4j.core-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [neo4j-clj.core :as db]
            [glam.neo4j.core :refer [init-db]])
  (:import (java.lang AutoCloseable)))

(def session nil)
(defn with-session
  "Fixture for creating a new session with a blank database"
  [f]
  (let [test-db (db/create-in-memory-connection)
        s ^AutoCloseable (db/get-session test-db)]
    (init-db test-db)
    (with-redefs [session s]
      (f))
    (.close s)))


