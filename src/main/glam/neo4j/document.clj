(ns glam.neo4j.document
  (:require [glam.neo4j.core :refer [defquery-nosquish]]))

(defquery-nosquish delete
                   "MATCH (d:Document {uuid: $uuid})
                   OPTIONAL MATCH (d)<-[*]-(n)
                   DETACH DELETE d,n")

