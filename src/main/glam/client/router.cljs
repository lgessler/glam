(ns glam.client.router
  (:require
    [cljs.spec.alpha :as s]
    [glam.client.application :refer [SPA]]
    [glam.client.prn-debug :refer [pprint-str]]
    [clojure.string :as str]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro.dom :as dom]
    [com.fulcrologic.guardrails.core :refer [>defn => | ? >def]]
    [dv.fulcro-util :as fu]
    [goog.object :as g]
    [reitit.core :as r]
    [reitit.frontend :as rf]
    [reitit.frontend.easy :as rfe]
    [taoensso.timbre :as log]))

(>def ::name keyword?)
(>def ::segment (s/or :vec (s/coll-of (s/or :s string? :k keyword?)) :fn fn?))
(>def ::route-map (s/keys :req-un [::name ::segment] :opt-un [::redirect-to]))
(>def ::route (s/cat :s string? :r ::route-map))

(defn map-vals [f m]
  (into {} (map (juxt key (comp f val))) m))

(>defn mk-routes-by-name
  "Returns a map like: {:root {:name :root :path '/'}}"
  [router]
  [some? => (s/map-of keyword? ::route-map)]
  (let [grouped (group-by (comp :name second) (r/routes router))]
    (map-vals
      ;; takes the path string and adds it as the key :path
      (fn [[[path-str prop-map]]]
        (assoc prop-map :path path-str))
      grouped)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Declare your app routes here
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; in a component use (r/route-segment :tasks) to ge the fulcro segment.

(def routes
  [["/"
    {:name :root
     :segment ["signup"]}]
   ["/projects"
    {:name :projects
     :segment ["projects"]}]
   #_["/projects/:slug"
    {:name :project
     :segment ["project"]
     :parameters {:path {:slug string?}}}]
   ["/tasks"
    {:name :tasks
     :segment ["tasks"]}]
   ["/signup"
    {:name :signup
     :segment ["signup"]}]])

;; example of possible redirect logic setup:
;   ["/goals" {:name :goals :redirect-to {:route :goals-date :params (fn [] {:date (t/today)})}}]

(def router (rf/router routes))
(def routes-by-name (mk-routes-by-name router))

(defn route-segment [name]
  (if-let [segment (some-> routes-by-name name :segment)]
    segment
    (throw (js/Error. (str "No matching fulcro segment for route: " (pr-str name))))))

(defn route-href [id]
  (let [conf (id routes-by-name)]
    (if-let [params (:params conf)]
      (rfe/href id params)
      (rfe/href id))))

(>defn url-path->vec
  "Return empty vector on no match, split url path on /"
  [path]
  [string? => (s/coll-of string? :kind vector?)]
  (let [s (->> (str/split path "/")
            (remove empty?)
            vec)]
    (if (seq s) s [])))

(def current-fulcro-route* (atom []))
(defn current-fulcro-route [] @current-fulcro-route*)

(def redirect-loop-count (volatile! 0))
(def max-redirect-loop-count 10)

;; Looks like the first match comes in as nil when init! is called.
;; read from the current url in that case.
;; not sure if nil is passed only if the current url doesn't match any
;; of your routes. Try it out.
(defn on-match
  [SPA router m]
  (log/info "on-match called with: " m)
  (let [m          (or m {:path (g/get js/location "pathname")})
        {:keys [path]} m
        has-match? (rf/match-by-path router path)]
    (log/info "router, match: " (pprint-str m))
    (if-not has-match?
      ;; unknown page, redirect to root
      (do
        (log/info "No fulcro route matched the current URL, changing to the default route.")
        (js/setTimeout #(rfe/push-state :root)))

      ;; route has redirect
      (if-let [{:keys [route params]} (get-in m [:data :redirect-to])]
        (let [params (params)]
          (do (log/info "redirecting to: " route " with params " params)
              (if (> @redirect-loop-count max-redirect-loop-count)
                (do
                  (log/error (str "The route " route " hit the max redirects limit: " max-redirect-loop-count))
                  (vreset! redirect-loop-count 0))
                (do
                  (vswap! redirect-loop-count inc)
                  (js/setTimeout #(rfe/replace-state route params))))))

        (let [segment         (-> m :data :segment)
              fulcro-segments (cond-> segment (fn? segment) (apply [m]))
              params          (:path-params m)
              ;; fill in any dynamic path segments with their values
              target-segment  (mapv (fn [part] (cond->> part (keyword? part) (get params))) fulcro-segments)]
          (log/info "Invoking Fulcro change route with " target-segment)
          (reset! current-fulcro-route* target-segment)
          (dr/change-route! SPA target-segment))))))

(defn init! [SPA]
  (log/info "Starting router.")
  (dr/initialize! SPA)
  (rfe/start!
    router
    (partial on-match SPA router)
    {:use-fragment false}))

(defn current-route [this]
  (some-> (dr/current-route this this) first keyword))

(defn current-app-route []
  (dr/current-route SPA))

(defn current-route-from-url []
  (rf/match-by-path router (g/get js/location "pathname")))

(defn current-route-name
  "Returns the keyword name of the current route as determined by the URL path."
  []
  (some-> (current-route-from-url) :data :name))

(defn route=url?*
  [route-key params { {curr-name :name} :data curr-params :path-params}]
  (boolean
    (when-let [{:keys [name]} (routes-by-name route-key)]
      (and
        (= name curr-name)
        (= params curr-params)))))

(defn route=url?
  "predicate does the :key like :goals {:date \"2020-05-20\"}
  match current reitit match of the url"
  [route-key params]
  (route=url?* route-key params (current-route-from-url)))

(comment (route=url? :goals {:date "2020-05-12"}))

(>defn change-route!
  ([route-key]
   [keyword? => any?]
   (let [{:keys [name] :as route} (get routes-by-name route-key)]
     (when-not (route=url? route-key {})
       (log/info "Changing route to: " route)
       (rfe/push-state name))))

  ([route-key params]
   [keyword? map? => any?]
   (let [{:keys [name] :as route} (get routes-by-name route-key)]
     (when-not (route=url? route-key params)
       (log/info "Changing route to : " route)
       (log/info "push state : " name " params: " params)
       (rfe/push-state name params)))))

(defn change-route-to-default! [this]
  (change-route! this :signup))

(defn change-route-after-signout! [this]
  (change-route! this :signup))

(defn link
  ([target]
   (link target {}))
  ([target opts]
   (dom/a :.item
     (merge
       {:classes [(when (= target (current-route-name)) "active")]
        :key     (str target)
        :href    (if (fu/on-server?)
                   (name target)
                   (route-href target))}
       opts)
     (str/capitalize (name target)))))
