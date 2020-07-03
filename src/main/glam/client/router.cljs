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

(defonce history (pushy/pushy
                   (fn [p]
                     (let [route-segments (vec (rest (str/split p "/")))
                           ;; extend special paths with a zero-length str
                           route-segments (if (#{"/" "/project/"} p)
                                            (conj route-segments "")
                                            route-segments)]
                       (log/info (str "changing route: " route-segments))
                       (dr/change-route! SPA route-segments)))
                   identity))

(defn route-to!
  "Change routes to the given route-string (e.g. \"/home\"."
  [route-string]
  (pushy/set-token! history route-string))

(defmutation route-to
  "Mutation to go to a specific route"
  [{:keys [route-string]}]
  (action [_]
          (route-to! route-string)))

(defn current-path
  []
  (g/get js/location "pathname"))

(defn link
  [path body]
  (dom/a :.item
         {:classes [(when (= path (current-path)) "active")]
          :href    path
          :onClick (fn [e]
                     (.preventDefault e)
                     (route-to! path))}
         body))

(defn init! [SPA]
  (log/info "Starting router.")
  (pushy/start! history)
  (dr/initialize! SPA))

