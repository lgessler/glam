(ns glam.neo4j.document
  (:require [neo4j-clj.core :as db]))

(db/defquery delete
  "MATCH (d:Document {uuid: $uuid})
  OPTIONAL MATCH (d)<-[*]-(n)
  DETACH DELETE d,n")

