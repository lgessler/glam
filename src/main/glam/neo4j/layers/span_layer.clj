(ns glam.neo4j.layers.span-layer
  (:require [neo4j-clj.core :as db]))

(db/defquery delete
  "MATCH (tl:TokenLayer {uuid: $uuid})
  OPTIONAL MATCH (tl)<-[rels*]-(n)
  WHERE NONE (r IN rels WHERE type(r) = \"NEXT_TEXT_LAYER\")
  DETACH DELETE tl,n")
