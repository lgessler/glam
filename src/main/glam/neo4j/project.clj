(ns glam.neo4j.project
  (:require [neo4j-clj.core :as db]
            [glam.neo4j.core :refer [defquery-nosquish defquery-nosquish-1 defquery-1 defquery]]))

(defquery-1 create
  "WITH randomUUID() AS uuid
  CREATE (p:Project {name: $name, slug: $slug, uuid: uuid})
  RETURN uuid")

(defquery get-all "MATCH (p:Project) RETURN p AS project")
(defquery-nosquish get-all-ids "MATCH (p:Project) RETURN p.uuid AS uuid")
(defquery-1 get-count "MATCH (p:Project) RETURN count(p) AS count")

(defquery-1 get-id-by-name "MATCH (p:Project) WHERE p.name = $name RETURN p.uuid AS uuid")
(defquery-1 get-id-by-slug "MATCH (p:Project) WHERE p.slug = $slug RETURN p.uuid AS uuid")

(defquery-1 get-props "MATCH (p:Project) WHERE p.uuid = $uuid RETURN p AS props")

(defquery set-name "MATCH (p:Project) WHERE p.uuid = $uuid SET p.name = $name")
(defquery set-slug "MATCH (p:Project) WHERE p.uuid = $uuid SET p.slug = $slug")
(defquery delete "MATCH (p:Project) WHERE p.uuid = $uuid DELETE p")

;; Cascading delete: don't just delete the project, delete EVERYTHING dependent on it (!)
(defquery delete
  "MATCH (p:Project {uuid: $uuid})
  OPTIONAL MATCH (p)<-[*]-(n)
  DETACH DELETE p,n")

;; project -> document
(defquery-1 add-document
  "WITH randomUUID() as uuid
  MATCH (p:Project {uuid: $project_uuid})
  CREATE (p)-[r:HAS_DOCUMENT]->(d:Document {name: $name, uuid: uuid})
  RETURN uuid")

(defquery get-document-ids
  "MATCH(p:Project {uuid: $uuid})-[:HAS_DOCUMENT]->(d:Document)
  RETURN d.uuid")

;; project -> layer
(defquery-1 add-text-layer
  "WITH randomUUID() as uuid
  MATCH (p:Project {uuid: $project_uuid})
  OPTIONAL MATCH (lastTextLayer:Layer:TextLayer)-[:TEXT_LAYER_OF]->(p)
  WHERE NOT (lastTextLayer)-[:NEXT_TEXT_LAYER]->()
  CREATE (textLayer:Layer:TextLayer {name: $name, uuid: uuid})-[r:TEXT_LAYER_OF]->(p)
  FOREACH (dummy in CASE WHEN lastTextLayer IS NOT NULL THEN [1] ELSE [] END
  | CREATE (lastTextLayer)-[:NEXT_TEXT_LAYER]->(textLayer))
  RETURN uuid")

(defquery get-text-layer-ids
  "MATCH (p:Project {uuid: $uuid})<-[:TEXT_LAYER_OF]-(textLayer:TextLayer)
  RETURN textLayer.uuid")


