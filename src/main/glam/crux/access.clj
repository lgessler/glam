(ns glam.crux.access
  (:require [crux.api :as crux]
            [taoensso.timbre :as log]))

(def key-symbol-map
  {:project/id     '?p
   :text-layer/id  '?txtl
   :token-layer/id '?tokl
   :span-layer/id  '?sl})

(defmulti build-query
  "Build up a query for finding whether a target piece of information is accessible for
  a given user. In the base case, the target is a project, and we inspect whether the user
  id is contained in :project/readers or :project/writers. Otherwise, we need to walk the
  graph from the target until we reach its associated project. This multimethod implements
  the crux rules necessary to support this traversal."
  (fn [query-map opts target-id] target-id))

(defmethod build-query :project/id [query-map {:keys [writeable]} _]
  ;; Base case--a project's accessible if its user is listed in writers or readers
  (-> query-map
      (update :where conj '(project-accessible ?p ?u))
      (update :rules conj ['(project-accessible ?p ?u)
                           (if writeable
                             '[?p :project/writers ?u]
                             '(or [?p :project/readers ?u]
                                  [?p :project/writers ?u]))])))

(defmethod build-query :text-layer/id [query-map opts _]
  (-> query-map
      (update :where conj '(text-layer-accessible ?txtl ?p))
      (update :rules conj '[(text-layer-accessible ?txtl ?p)
                            [?p :project/text-layers ?txtl]])
      (build-query opts :project/id)))

(defmethod build-query :token-layer/id [query-map opts _]
  (-> query-map
      (update :where conj '(token-layer-accessible ?tokl ?txtl))
      (update :rules conj '[(token-layer-accessible ?tokl ?txtl)
                            [?txtl :text-layer/token-layers ?tokl]])
      (build-query opts :text-layer/id)))

(defmethod build-query :span-layer/id [query-map opts _]
  (-> query-map
      (update :where conj '(span-layer-accessible ?sl ?tokl))
      (update :rules conj '[(span-layer-accessible ?sl ?tokl)
                            [?tokl :token-layer/span-layers ?sl]])
      (build-query opts :token-layer/id)))

(defn get-accessible-ids
  "Get all accessible IDs of a certain type given a user's privileges on projects.
  `target-key` is something like :project/id"
  [node user-id target-key]
  (let [query (build-query {:find  [(target-key key-symbol-map)]
                            :where [['?u :user/id user-id]]
                            :rules []}
                           {:writeable false}
                           target-key)]
    (map first (crux/q (crux/db node) query))))

(defn ident-readable?
  "Test whether a given ident is readable for a given user."
  [node user-id [target-key target-id]]
  (or
    (-> node crux/db (crux/entity user-id) :user/admin?)
    (let [query (build-query {:find  ['?target]
                              :where [['?u :user/id user-id]
                                      ['?target target-key target-id]
                                      [(list '= '?target (get key-symbol-map target-key))]]
                              :rules []}
                             {:writeable false}
                             target-key)]
      (not (empty? (crux/q (crux/db node) query))))))

(defn ident-writeable?
  "Test whether a given ident is writeable for a given user."
  [node user-id [target-key target-id]]
  (or
    (-> node crux/db (crux/entity user-id) :user/admin?)
    (let [query (build-query {:find  ['?target]
                              :where [['?u :user/id user-id]
                                      ['?target target-key target-id]
                                      [(list '= '?target (get key-symbol-map target-key))]]
                              :rules []}
                             {:writeable true}
                             target-key)]
      (not (empty? (crux/q (crux/db node) query))))))

(comment
  (build-query {:find '[?p] :where [['?u :user/id 1]] :rules []} :text-layer/id))
