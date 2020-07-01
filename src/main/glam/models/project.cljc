(ns glam.models.project
  (:require #?(:clj [glam.neo4j.project :as prj])
            #?(:clj [glam.neo4j.core :refer [one ones]])
            #?(:clj [com.wsscode.pathom.connect :as pc])
            #?(:clj [clojure.set :refer [rename-keys]])))

(def keymap
  {:uuid :project/id
   :name :project/name})

#?(:clj
   (pc/defresolver all-projects [{:keys [neo4j current-user]} _]
     {:com.wsscode.pathom.connect/output [{:all-projects [:project/id]}]}
     {:all-projects (as-> neo4j $
                          (prj/get-all $)
                          (ones $)
                          (map #(rename-keys % keymap) $)
                          (vec $))}))

#?(:clj
   (pc/defresolver get-project [{:keys [neo4j]} {:project/keys [id]}]
     {:com.wsscode.pathom.connect/input  #{:project/id}
      :com.wsscode.pathom.connect/output [:project/name]}
     (-> neo4j
         (prj/get-props {:project_uuid id})
         one
         (rename-keys keymap))))

#?(:clj
   (def project-resolvers [all-projects get-project]))


