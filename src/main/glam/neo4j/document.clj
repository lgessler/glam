(ns glam.neo4j.document
  (:require [neo4j-clj.core :as db]))

;; create
(db/defquery create
  "WITH randomUUID() AS uuid
  CREATE (d:Document {name: $name, uuid: uuid})
  RETURN uuid")

;; multiple lookup
(db/defquery get-all "MATCH (d:Document) RETURN d as document, id(d) as id")
(db/defquery get-count "MATCH (d:Document) RETURN count(d) as count")


