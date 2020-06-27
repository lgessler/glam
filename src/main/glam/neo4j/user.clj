(ns glam.neo4j.user
  (:require [neo4j-clj.core :as db]))

;; create
(db/defquery create
  "WITH randomUUID() AS uuid
  CREATE (u:User {name: $name, password_hash: $password_hash, uuid: uuid})
  RETURN uuid")

;; multiple lookup
(db/defquery get-all "MATCH (u:User) RETURN u AS user")
(db/defquery get-count "MATCH (u:User) RETURN count(u) AS count")

;; lookup
(db/defquery get-id-by-name "MATCH (u:User) WHERE u.name = $name RETURN u.uuid AS uuid")
(db/defquery get-id-by-email "MATCH (u:User) WHERE u.email = $email RETURN u.uuid AS uuid")

;; gets
(db/defquery get-props "MATCH (u:User) WHERE u.uuid = $uuid RETURN u AS props")

;; sets
(db/defquery set-name "MATCH (u:User) WHERE u.uuid = $uuid SET u.name = $name")
(db/defquery set-email "MATCH (u:User) WHERE u.uuid = $uuid SET u.email = $email")
(db/defquery set-password-hash "MATCH (u:User) WHERE u.uuid = $uuid SET u.password_hash = $password_hash")
(db/defquery set-admin "MATCH (u:User) WHERE u.uuid = $uuid SET u.admin = $admin")

;; delete
(db/defquery delete "MATCH (u:User) WHERE u.uuid = $uuid DELETE u")

;; project -> user
;; Projects depend on Users because when Projects are deleted
;; all nodes pointing at the Project are recursively deleted
(db/defquery set-reader
  "MATCH (u:User), (p:Project)
  WHERE u.uuid = $user_uuid AND p.uuid = $project_uuid
  CREATE (p)-[r:READ_ACCESS]->(u)
  RETURN type(r)")

(db/defquery set-writer
  "MATCH (u:User), (p:Project)
  WHERE u.uuid = $user_uuid AND p.uuid = $project_uuid
  CREATE (p)-[r:WRITE_ACCESS]->(u)
  RETURN type(r)")

(db/defquery revoke-reader
  "MATCH (p:Project)-[r:READ_ACCESS]->(u:User)
  WHERE u.uuid = $user_uuid AND p.uuid = $project_uuid
  DELETE r")

(db/defquery revoke-writer
  "MATCH (p:Project)-[r:WRITE_ACCESS]->(u:User)
  WHERE u.uuid = $user_uuid AND p.uuid = $project_uuid
  DELETE r")

(db/defquery readable-projects
  "MATCH (p:Project)-[r:WRITE_ACCESS|READ_ACCESS]->(u:User {uuid: $uuid})
  RETURN p.uuid")

(db/defquery writeable-projects
  "MATCH (p:Project)-[r:WRITE_ACCESS]->(u:User {uuid: $uuid})
  RETURN p.uuid")