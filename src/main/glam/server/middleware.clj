(ns glam.server.middleware
  (:require
    [clojure.java.io :as io]
    [clojure.tools.reader.edn :as edn]
    [mount.core :as mount]
    [taoensso.timbre :as log]
    [hiccup.page :refer [html5]]
    [com.fulcrologic.fulcro.server.api-middleware :refer [handle-api-request wrap-transit-params wrap-transit-response]]
    [ring.util.response :as resp :refer [response file-response resource-response]]
    [ring.middleware.defaults :refer [wrap-defaults]]
    [xtdb-inspector.core :refer [inspector-handler]]
    [glam.server.config :refer [config]]
    [glam.server.pathom-parser :refer [parser mutation?]]
    [glam.server.xtdb :refer [xtdb-node]]
    [glam.server.session-store :refer [session-store]]
    [glam.server.rest-api.core :refer [rest-handler]])
  (:import (java.io PushbackReader IOException)))

(def manifest-file "public/js/main/manifest.edn")

(defn load-edn
  "Loads EDN. Tries to read input as resource first, then as filename.
  We need this to read shadow-cljs's manifest.edn file."
  [source]
  (try
    (with-open [r (io/reader (io/resource source))] (edn/read (PushbackReader. r)))
    (catch IOException e
      (try
        ;; try just a file read
        (with-open [r (io/reader source)] (edn/read (PushbackReader. r)))
        (catch IOException e
          (printf "Couldn't open '%s': %s\n" source (.getMessage e)))
        (catch RuntimeException e
          (printf "Error parsing edn file '%s': %s\n" source (.getMessage e))))
      (printf "Couldn't open '%s': %s\n" source (.getMessage e)))
    (catch RuntimeException e
      (printf "Error parsing edn file '%s': %s\n" source (.getMessage e)))))

(defn get-js-filename []
  (:output-name (first (load-edn manifest-file))))

(defn index
  "Dynamically generated HTML using hiccup. This makes it easy to dynamically embed the CSRF token, which
  will be used by Fulcro to set `X-CSRF-Header` in requests. During development, it also makes it easy
  for us to use whatever JS filename shadow-cljs is compiling to."
  [csrf-token]
  (if-let [js-filename (get-js-filename)]
    (do (log/debug "Serving index.html")
        (html5
          [:head {:lang "en"}
           [:title "Application"]
           [:meta {:charset "utf-8"}]
           [:meta {:name "viewport" :content "width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no"}]
           [:link {:rel "stylesheet" :href "https://fonts.googleapis.com/css?family=Roboto:300,400,500,700&display=swap"}]
           [:link {:rel "stylesheet" :href "https://fonts.googleapis.com/icon?family=Material+Icons"}]
           [:link {:rel "shortcut icon" :href "data:image/x-icon;," :type "image/x-icon"}]
           [:script (str "var fulcro_network_csrf_token = '" csrf-token "';")]]
          [:body
           [:div#app]
           [:script {:src (str "/js/main/" js-filename)}]]))
    (throw (Exception. (str "Error reading JavaScript filename from shadow-cljs manifest.edn file.
    The filename is nil, you probably need to wait for the shadow-cljs build to complete or start it again.
    Check manifest.edn in the shadow-cljs build directory.")))))

;; Route handling
(defn wrap-html-routes
  "Allows all requests to `/api` to continue down the handler chain, and otherwise terminates
  the handler chain by serving the index page. This allows page refresh to work 'right' from
  a user's perspective, as refreshing on e.g. `/projects/` will simply serve the index, after
  which the client-side routing setup will ensure that the proper components are displayed."
  [ring-handler parser]
  (let [rest-handler (rest-handler parser)]
    (fn [{:keys [uri anti-forgery-token] :as req}]
      (cond
        (re-matches #"^/rest-api/.*" uri)
        (rest-handler req)

        (re-matches #"^/api" uri)
        (ring-handler req)

        :else
        (-> (resp/response (index anti-forgery-token))
            (resp/content-type "text/html")
            (resp/set-cookie "csrf-token" anti-forgery-token {:http-only true :path "/" :same-site :strict}))))))

(defn wrap-ajax-api
  "AJAX remote for Fulcro, registered on the client as the :remote remote"
  [parser]
  (fn [request]
    (handle-api-request
      (:transit-params request)
      (fn [tx] (parser {:ring/request request} tx)))))

(defn wrap-xtdb-inspector
  "For development only. Enable a web ui for inspecting DB state if :glam.server.xtdb/config is true."
  [ring-handler]
  (let [inspector? (and (-> config :glam.server.xtdb/config :use-inspector))
        handler (and inspector? (inspector-handler xtdb-node))]
    (fn [request]
      (if (and inspector?
               (map? request)
               (-> request :uri (clojure.string/split #"/") (get 1) (get 0) (= \_)))
        (handler request)
        (ring-handler request)))))

;; Override ring anti forgery's way of getting the CSRF token
(defn- form-params [request]
  (merge (:form-params request)
         (:multipart-params request)))
(defn- read-token [request]
  (or (-> request form-params (get "__anti-forgery-token"))
      (-> request :cookies (get "csrf-token") :value)))

(mount/defstate middleware
  :start
  (let [defaults-config (:ring.middleware/defaults-config config)]
    (-> (wrap-ajax-api parser)
        wrap-transit-params
        wrap-transit-response
        (wrap-html-routes parser)
        wrap-xtdb-inspector
        (wrap-defaults (-> defaults-config
                           (assoc :session {:store session-store})
                           (assoc-in [:security :anti-forgery] {:read-token read-token}))))))
