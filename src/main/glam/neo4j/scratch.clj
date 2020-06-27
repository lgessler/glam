(ns glam.neo4j.scratch
  (:require [neo4j-clj.core :as db]
            [glam.neo4j.core :as core]
            [glam.neo4j.user :as user]
            [glam.neo4j.project :as project]
            [glam.neo4j.document :as document]))

(comment

  (do
    (def conn (db/connect (java.net.URI. "neo4j://localhost:7687") "neo4j" "password"))
    (def s (db/get-session conn))
    (db/execute s "MATCH (n) DETACH DELETE (n)")

    (db/execute s "CREATE (t:Text {name: \"foo\"})")
    (db/execute s "MATCH (t:Text) CREATE (a:A {name: \"a1\"})-[r:X]->(t)")
    (db/execute s "MATCH (t:Text) CREATE (a:A {name: \"a2\"})-[r:X]->(t)")
    (db/execute s "MATCH (a:A {name: \"a1\"}) CREATE (b:B {name: \"b1\"})-[r:Y]->(a)")
    (db/execute s "MATCH (a:A {name: \"a1\"}) CREATE (b:B {name: \"b2\"})-[r:Y]->(a)")
    (db/execute s "MATCH (a:A {name: \"a1\"}) CREATE (b:B {name: \"b3\"})-[r:Y]->(a)")
    (db/execute s "MATCH (a:A {name: \"a2\"}) CREATE (b:B {name: \"b4\"})-[r:Y]->(a)")

    (db/execute s "
    MATCH (a:A {name: \"a1\"})
    OPTIONAL MATCH (x)-[:X|Y*]->(a)
    DETACH DELETE x,a")
    )

  (do
    (def conn (db/create-in-memory-connection))
    (def conn (db/connect (java.net.URI. "neo4j://localhost:7687") "neo4j" "password"))
    (def s (db/get-session conn))

    (db/execute s "MATCH (n) DETACH DELETE (n)")

    (def project-id (core/one (project/create s {:name "test"})))
    project-id

    (def doc1-id (project/add-document s {:projectUuid project-id :name "doc1"}))
    (def doc2-id (project/add-document s {:projectUuid project-id :name "doc2"}))

    (def document-id (core/one (project/add-document s {:projectId project-id :name "doc1"})))
    document-id

    (db/execute s "MATCH (n) RETURN n,labels(n) ")

    (db/execute s "RETURN apoc.create.uuid() AS uuid")


    )

  )



