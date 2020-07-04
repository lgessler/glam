(ns glam.models.project
  (:require [glam.neo4j.project :as prj]
            [com.wsscode.pathom.connect :as pc]
            [clojure.set :refer [rename-keys]]
            [taoensso.timbre :as log]))

(def keymap
  {:uuid :project/id
   :name :project/name
   :slug :project/slug})

;; TODO: return only projects that the user has auth for
(pc/defresolver all-projects [{:keys [neo4j current-user]} _]
  {::pc/output [{:all-projects [:project/id]}]}
  {:all-projects (->> neo4j
                      prj/get-all-ids
                      (map #(rename-keys % keymap))
                      vec)})

(pc/defresolver project-slugs [{:keys [neo4j]} _]
  {::pc/output [{:project-slugs [:project/id :project/slug]}]}
  (try
    (->> neo4j
         prj/get-all
         (map #(rename-keys % keymap))
         (map #(select-keys % [:project/id :project/slug]))
         vec
         (println "OK"))
    (catch Exception e (log/error (.getMessage e))))
  {:project-slugs (->> neo4j
                       prj/get-all
                       (map #(rename-keys % keymap))
                       (map #(select-keys % [:project/id :project/slug]))
                       (map (fn [{:project/keys [id] :as props}]
                              (assoc props :project/id [:project/id id])))
                       vec)})

(pc/defresolver get-project-by-slug [{:keys [neo4j]} {:project/keys [slug]}]
  {::pc/input  #{:project/slug}
   ::pc/output [{:project/id [:project/id]}]}
  (-> neo4j
      (prj/get-id-by-slug {:slug slug})
      (rename-keys keymap)
      ((fn [{:project/keys [id] :as props}]
         (assoc props :project/id [:project/id id])))))

(pc/defresolver get-project [{:keys [neo4j]} {:project/keys [id]}]
  {::pc/input  #{:project/id}
   ::pc/output [:project/id :project/name :project/slug]}
  (-> neo4j
      (prj/get-props {:uuid id})
      (rename-keys keymap)))

(def project-resolvers [all-projects get-project])

