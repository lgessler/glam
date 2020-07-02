(ns glam.neo4j.layers.text-layer
  (:require [glam.neo4j.core :refer [defquery defquery-1]]))

(defquery delete
  "MATCH (tl:TextLayer {uuid: $uuid})
  OPTIONAL MATCH (tl)<-[rels*]-(n)
  WHERE NONE (r IN rels WHERE type(r) = \"NEXT_TEXT_LAYER\")
  DETACH DELETE tl,n")

(defquery-1 add-token-layer
  "WITH randomUUID() as uuid
  MATCH (textLayer:Layer:TextLayer {uuid: $text_layer_uuid})
  OPTIONAL MATCH (lastTokenLayer:Layer:TokenLayer)-[:TOKEN_LAYER_OF]->(textLayer)
  WHERE NOT (lastTokenLayer)-[:NEXT_TOKEN_LAYER]->()
  CREATE (tokenLayer:Layer:TokenLayer {name: $name, uuid: uuid})-[r:TOKEN_LAYER_OF]->(textLayer)
  FOREACH (dummy in CASE WHEN lastTokenLayer IS NOT NULL THEN [1] ELSE [] END
  | CREATE (lastTokenLayer)-[:NEXT_TOKEN_LAYER]->(tokenLayer))
  RETURN uuid")