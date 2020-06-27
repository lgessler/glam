(ns glam.server.pathom-playground
  (:require
    [clojure.core.async :as async :refer [go <! >! <!! chan]]
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [clojure.pprint :refer [pprint]]
    [clojure.spec.alpha :as s]
    [com.wsscode.pathom.connect :as pc]
    [com.wsscode.pathom.core :as p]
    [dv.crux-util :as cutil]
    [edn-query-language.core :as eql]
    [mount.core :refer [defstate]]
    [taoensso.timbre :as log]
    [glam.auth.user :as user]
    [glam.server.pathom-parser :refer [parser]])
  (:import [java.io PushbackReader IOException]))

(defn with-user
  ([email query]
   (parser {:ring/request {:session {:user/name email :session/valid? true}}}
     query))
  ([query]
   (parser {:ring/request {:session {:user/name "abc@abc.com" :session/valid? true}}}
     query)))

(comment
  (with-user "my-user@test.com"
    [:task/description])
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Playground parser
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Recursive resolver example

(pc/defresolver things-res [_ {:keys [crux.db/id]}]
  {::pc/input  #{:crux.db/id}
   ::pc/output [:name :children :crux.db/id]}
  (let [ent (cutil/entity id)
        ent (assoc ent :children (mapv #(hash-map :crux.db/id %) (:children ent)))]
    (log/info "Found entity: " ent)
    (select-keys ent [:name :children :crux.db/id])))

(def resolvers [things-res])

(def myparser
  (p/parser {::p/env     {::p/reader               [p/map-reader pc/reader2 pc/open-ident-reader
                                                    p/env-placeholder-reader]
                          ::p/placeholder-prefixes #{">"}}
             ::p/mutate  pc/mutate
             ::p/plugins [(pc/connect-plugin {::pc/register resolvers})
                          p/error-handler-plugin
                          p/trace-plugin]}))


