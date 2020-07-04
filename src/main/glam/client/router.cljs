(ns glam.client.router
  (:require
    [cljs.spec.alpha :as s]
    [glam.client.application :refer [SPA]]
    [glam.client.prn-debug :refer [pprint-str]]
    [clojure.string :as str]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.dom :as dom]
    [com.fulcrologic.guardrails.core :refer [>defn => | ? >def]]
    [dv.fulcro-util :as fu]
    [goog.object :as g]
    [taoensso.timbre :as log]
    [pushy.core :as pushy]))

;; keep track of routes here for convenience
;; (reitit or a similar library would be best here but i found it confusing to use)
(def routes
  {:signup   "/"
   :settings "/settings"
   :projects "/project/"
   :project  "/project/:id"})

(defn resolve-route
  ([route-key]
   (resolve-route route-key {}))
  ([route-key params]
   (if-let [route-string (route-key routes)]
     (let [route-string (reduce (fn [s [k v]]
                                  (clojure.string/replace s (str k) v))
                                route-string
                                params)]
       (log/info "Resolved " (pr-str route-key) " with params " (pr-str params) " to " route-string)
       route-string)
     (ex-info "Couldn't resolve route key into a route" route-key))))

(defonce history
         (pushy/pushy
           (fn [p]
             (let [route-segments (vec (rest (str/split p "/")))
                   ;; extend special paths with a zero-length str
                   route-segments (if (and (some (hash-set p) (vals routes))
                                           (= (last p) \/))
                                    (conj route-segments "")
                                    route-segments)]
               (log/info (str "changing route: " route-segments))
               (dr/change-route! SPA route-segments)))
           identity))

(defn route-to!
  "Change routes to the given route-string (e.g. \"/home\"."
  ([route-key]
   (route-to! route-key {}))
  ([route-key path-params]
   (pushy/set-token! history (resolve-route route-key))))

(defmutation route-to
  "Mutation to go to a specific route"
  [{:keys [route-key]}]
  (action [_]
          (route-to! (resolve-route route-key))))

(defn current-path
  []
  (g/get js/location "pathname"))

(defn link
  [route-key params body]
  (let [route-string (resolve-route route-key params)]
    (dom/a :.item
           {:classes [(when (= route-string (current-path)) "active")]
            :href    route-string}
           body)))

(defn init! [SPA]
  (log/info "Starting router.")
  (pushy/start! history)
  (dr/initialize! SPA))

