(ns glam.neo4j.project-test
  (:require [clojure.test :refer :all]
            [glam.neo4j.project :as prj]
            [glam.neo4j.document :as doc]
            [glam.neo4j.core-test :refer [session with-session]]
            [glam.neo4j.user :as user]
            [neo4j-clj.core :as db])
  (:import (org.neo4j.driver.exceptions ClientException)))

(defn with-user
  [f]
  (let [uuid (user/create session {:name "luke" :email "a@b.com" :password_hash "secret"})]
    (f)
    (user/delete session {:uuid uuid})))

(defn with-project
  [f]
  (let [uuid (prj/create session {:name "existing" :slug "existing"})]
    (f)
    (prj/delete session {:uuid uuid})))

(use-fixtures :once with-session with-user with-project)

(deftest create
  (let [uuid (prj/create session {:name "test" :slug "test"})]
    (is (= 2 (count (prj/get-all session))))
    (prj/delete session {:uuid uuid})))

(deftest permissions
  (let [prj-uuid (prj/get-id-by-name session {:name "existing"})
        user-uuid (user/get-id-by-name session {:name "luke"})]
    (user/set-writer session {:user_uuid user-uuid :project_uuid prj-uuid})
    (is (= 1
           (count
             (db/execute session
                         "MATCH (u:User {uuid: $user_uuid})
             <-[r:WRITE_ACCESS]-(p:Project {uuid: $project_uuid}) RETURN r"
                         {:user_uuid user-uuid :project_uuid prj-uuid}))))
    (user/revoke-writer session {:user_uuid user-uuid :project_uuid prj-uuid})))

(deftest add-text-document
  (let [prj-uuid (prj/get-id-by-name session {:name "existing"})
        doc-uuid (prj/add-document session {:project_uuid prj-uuid :name "doc1"})]
    (is (= 1 (count (prj/get-document-ids session {:uuid prj-uuid}))))
    (doc/delete session {:uuid doc-uuid})
    (is (= 0 (count (prj/get-document-ids session {:uuid prj-uuid}))))
    ))