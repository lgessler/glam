(ns glam.neo4j.project-test
  (:require [clojure.test :refer :all]
            [glam.neo4j.core :refer [one]]
            [glam.neo4j.project :as prj]
            [glam.neo4j.core-test :refer [session with-session]]
            [glam.neo4j.user :as user]
            [neo4j-clj.core :as db])
  (:import (org.neo4j.driver.exceptions ClientException)))

(defn with-user
  [f]
  (let [uuid (one (user/create session {:name "luke" :password_hash "secret"}))]
    (f)
    (user/delete session {:uuid uuid})))

(defn with-project
  [f]
  (let [uuid (one (prj/create session {:name "existing"}))]
    (f)
    (prj/delete session {:uuid uuid})))

(use-fixtures :once with-session with-user with-project)

(deftest create
  (let [uuid (one (prj/create session {:name "test"}))]
    (is (= 2 (count (prj/get-all session))))
    (prj/delete session {:uuid uuid})))

(deftest permissions
  (let [prj-uuid (one (prj/get-id-by-name session {:name "existing"}))
        user-uuid (one (user/get-id-by-name session {:name "luke"}))]
    (user/set-writer session {:user_uuid user-uuid :project_uuid prj-uuid})
    (is (= 1
           (count
             (db/execute session
                         "MATCH (u:User {uuid: $user_uuid})
             <-[r:WRITE_ACCESS]-(p:Project {uuid: $project_uuid}) RETURN r"
                         {:user_uuid user-uuid :project_uuid prj-uuid}))))
    (user/revoke-writer session {:user_uuid user-uuid :project_uuid prj-uuid})))