(ns glam.client.application
  (:require
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.networking.http-remote :as net]
    [sablono.core :as html :refer-macros [html]]
    [taoensso.timbre :as log]))

(goog-define LOG-RESPONSES false)

(defn wrap-accept-transit
  "For servers that do content negotiation, tell them we want transit+json"
  [handler]
  (fn [req]
    (handler (assoc-in req [:headers "Accept"] "application/transit+json"))))

(defn get-token []
  (if (exists? js/fulcro_network_csrf_token)
    js/fulcro_network_csrf_token
    "TOKEN-NOT-IN-HTML!"))

(defn request-middleware []
  (js/console.log "js/fulcro_network_csrf_token: " (get-token))
  ;; The CSRF token is embedded via service.clj
  (->
    (net/wrap-csrf-token (get-token))
    (net/wrap-fulcro-request)
    (wrap-accept-transit)))

;; To view the map response as a map of data uncomment this:
(defn resp-logger [handler]
  (fn [resp]
    (let [out (handler resp)]
      (log/info "\nRESPONSE: ")
      ;(log/info "RESP_LOGGER: ") (log/info (with-out-str (pprint out)))
      out)))

(defn response-middleware []
  (cond-> (net/wrap-fulcro-response)
    LOG-RESPONSES resp-logger))

(defn api-remote []
  (net/fulcro-http-remote
    {:url                 "/api"
     :response-middleware (response-middleware)
     :request-middleware  (request-middleware)}))

(defn mutation?
  [body]
  (and
    (map? body)
    (-> body keys first symbol?)))

(defn mutation-error? [result]
  (let [body         (:body result)
        is-mutation? (mutation? body)]
    (if is-mutation?
      (let [mutation-sym    (-> body keys first)
            response-error? (-> body mutation-sym :server/error?)
            pathom-error    (-> body mutation-sym :com.wsscode.pathom.core/reader-error)]
        (log/info "Result body: " body)
        (boolean (or response-error? pathom-error)))
      false)))

(defn read-error? [result]
  (let [body (:body result)]
    (and (map? body)
      (= (-> body keys first body) :com.wsscode.pathom.core/reader-error))))

(defn remote-error?
  [result]
  (let [status (:status-code result)
        ;; todo add support for read errors if :server/error? is present in top level
        ;; in pathom use similar helper as augment-session-resp in session to
        ;; override the resp body to just be the map.
        ;;
        resp   (or (not= status 200) (mutation-error? result)
                 (read-error? result))]
    (log/info "Remote error? " resp)
    resp))

(defonce SPA
  (app/fulcro-app
    {:render-middleware (fn [this render] (html (render)))
     :remote-error?     remote-error?
     :remotes           {:remote (api-remote)}}))
