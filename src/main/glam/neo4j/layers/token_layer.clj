(ns glam.neo4j.layers.token-layer
  (:require [glam.neo4j.core :refer [defquery defquery-1]]))


(defquery delete
  "MATCH (tl:TokenLayer {uuid: $uuid})
  OPTIONAL MATCH (tl)<-[rels*]-(n)
  WHERE NONE (r IN rels WHERE type(r) = \"NEXT_TOKEN_LAYER\")
  DETACH DELETE tl,n")

(defquery-1 add-span-layer
  "WITH randomUUID() as uuid
  MATCH (tokenLayer:Layer:TokenLayer {uuid: $token_layer_uuid})
  OPTIONAL MATCH (lastSpanLayer:Layer:SpanLayer)-[:SPAN_LAYER_OF]->(tokenLayer)
  WHERE NOT (lastSpanLayer)-[:NEXT_SPAN_LAYER]->()
  CREATE (spanLayer:Layer:SpanLayer {name: $name, uuid: uuid})-[r:SPAN_LAYER_OF]->(tokenLayer)
  FOREACH (dummy in CASE WHEN lastSpanLayer IS NOT NULL THEN [1] ELSE [] END
  | CREATE (lastSpanLayer)-[:NEXT_SPAN_LAYER]->(spanLayer))
  RETURN uuid")
