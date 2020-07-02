(ns glam.neo4j.user-test
  (:require [clojure.test :refer :all]
            [glam.neo4j.user :as user]
            [glam.neo4j.core-test :refer [session with-session]])
  (:import (org.neo4j.driver.exceptions ClientException)))

(defn with-users
  [f]
  (let [uuid1 (user/create session {:name "ldg" :password_hash "topsecret"})
        uuid2 (user/create session {:name "fdr" :password_hash "othersecret"})]
    (user/set-email session {:uuid uuid2 :email "foo@bar.com"})
    (f)
    (user/delete session {:uuid uuid1})
    (user/delete session {:uuid uuid2})))

(use-fixtures :once with-session)
(use-fixtures :each with-users)

(deftest create
  (testing "Users can be created and they return their ID"
    (let [uuid (user/create session {:name "amr" :password_hash "s3cr37"})
          user-count (user/get-count session)]
      (is (string? uuid))
      (is (= user-count 3))
      (user/delete session {:uuid uuid})))

  (testing "Users must have a name and password_hash"
    (is (thrown? ClientException (user/create session {:password_hash "topsecret"})))
    (is (thrown? ClientException (user/create session {:name "ldg"})))
    (is (thrown? ClientException (user/create session {}))))

  (testing "Extra keys are ignored"
    (let [uuid (user/create session {:name          "alice"
                                     :password_hash "secret"
                                     :email         "fdr@vidaliaonions.com"})]
      (is (nil? (-> (user/get-props session {:uuid uuid}) :user :email)))
      (user/delete session {:uuid uuid}))))

(deftest get-all
  (testing "Return all users"
    (is (= (count (user/get-all session)) 2))))

(deftest get-count
  (testing "Return number of users"
    (is (= (user/get-count session) 2))))

(deftest get-id-by-name
  (testing "User identifiable by name"
    (is (some? (user/get-id-by-name session {:name "ldg"})))
    (is (nil? (user/get-id-by-name session {:name "nobody"})))))

(deftest get-id-by-email
  (testing "User identifiable by name"
    (is (some? (user/get-id-by-email session {:email "foo@bar.com"})))
    (is (nil? (user/get-id-by-email session {:email "nobody@nowhere.com"})))))

(deftest get-props
  (testing "All props returned"
    (is (= {:name "ldg" :password_hash "topsecret"}
           (let [uuid (user/get-id-by-name session {:name "ldg"})]
             (-> (user/get-props session {:uuid uuid})
                 (dissoc :uuid)))))))

(deftest set-name
  (testing "name modifiable"
    (let [uuid (user/get-id-by-name session {:name "ldg"})]
      (user/set-name session {:uuid uuid :name "gdl"})
      (is (= "gdl"
             (:name (user/get-props session {:uuid uuid}))))
      (user/set-name session {:uuid uuid :name "ldg"}))))

(deftest set-email
  (testing "email modifiable"
    (let [uuid (user/get-id-by-name session {:name "ldg"})]
      (user/set-email session {:uuid uuid :email "ldg@vidaliaonions.com"})
      (is (= "ldg@vidaliaonions.com"
             (:email (user/get-props session {:uuid uuid})))))))

(deftest set-password-hash
  (testing "password_hash modifiable"
    (let [uuid (user/get-id-by-name session {:name "ldg"})]
      (user/set-password-hash session {:uuid uuid :password_hash "s00p3rsecre7!"})
      (is (= "s00p3rsecre7!"
             (:password_hash (user/get-props session {:uuid uuid})))))))

(deftest user-constraints
  (let [uuid-alice (user/create session {:name "alice" :password_hash "foo"})]
    (testing "Unique name"
      (is (thrown? ClientException (user/create session {:name "alice" :password_hash "bar"}))))

    (testing "Unique email"
      (user/set-email session {:uuid uuid-alice :email "alice@vidaliaonions.com"})
      (let [uuid-alice2 (user/create session {:name "alice2" :password_hash "bar"})]
        (is (thrown? ClientException (user/set-email session {:uuid uuid-alice2 :email "alice@vidaliaonions.com"})))
        (user/delete session {:uuid uuid-alice})
        (user/delete session {:uuid uuid-alice2})))))

(deftest delete
  (testing "users are deletable"
    (let [uuid (user/get-id-by-name session {:name "ldg"})]
      (user/delete session {:uuid uuid})
      (is (= 0
             (count (user/get-id-by-name session {:name "ldg"})))))))