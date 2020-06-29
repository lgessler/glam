(ns glam.neo4j.project
  (:require [neo4j-clj.core :as db]))

(db/defquery create
  "WITH randomUUID() AS uuid
  CREATE (p:Project {name: $name, uuid: uuid})
  RETURN uuid")

(db/defquery get-all "MATCH (p:Project) RETURN p AS project")
(db/defquery get-count "MATCH (p:Project) RETURN count(p) AS count")

(db/defquery get-id-by-name "MATCH (p:Project) WHERE p.name = $name RETURN p.uuid AS uuid")

(db/defquery get-props "MATCH (p:Project) WHERE p.uuid = $uuid RETURN p AS props")

(db/defquery set-name "MATCH (p:Project) WHERE p.uuid = $uuid SET p.name = $name")
(db/defquery delete "MATCH (p:Project) WHERE p.uuid = $uuid DELETE p")

;; Cascading delete: don't just delete the project, delete EVERYTHING dependent on it (!)
(db/defquery delete
  "MATCH (p:Project {uuid: $uuid})
  OPTIONAL MATCH (p)<-[*]-(n)
  DETACH DELETE p,n")


;; project -> document
(db/defquery add-document
  "WITH randomUUID() as uuid
  MATCH (p:Project {uuid: $project_uuid})
  CREATE (p)-[r:HAS_DOCUMENT]->(d:Document {name: $name, uuid: uuid})
  RETURN uuid")

(db/defquery get-document-ids
  "MATCH(p:Project {uuid: $uuid})-[:HAS_DOCUMENT]->(d:Document)
  RETURN d.uuid")

;; project -> layer
(db/defquery add-text-layer
  "WITH randomUUID() as uuid
  MATCH (p:Project {uuid: $project_uuid})
  OPTIONAL MATCH (lastTextLayer:Layer:TextLayer)-[:TEXT_LAYER_OF]->(p)
  WHERE NOT (lastTextLayer)-[:NEXT_TEXT_LAYER]->()
  CREATE (textLayer:Layer:TextLayer {name: $name, uuid: uuid})-[r:TEXT_LAYER_OF]->(p)
  FOREACH (dummy in CASE WHEN lastTextLayer IS NOT NULL THEN [1] ELSE [] END
  | CREATE (lastTextLayer)-[:NEXT_TEXT_LAYER]->(textLayer))
  RETURN uuid")

(db/defquery get-text-layer-ids
  "MATCH (p:Project {uuid: $uuid})<-[:TEXT_LAYER_OF]-(textLayer:TextLayer)
  RETURN textLayer.uuid")


#_(db/execute s "MATCH (n:Project) CREATE (d:Document {name: \"foo\"})-[:HAS]->(n) RETURN id(d)")

