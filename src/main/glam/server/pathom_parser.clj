(ns glam.server.pathom-parser
  (:require [com.wsscode.pathom.connect :as pc]
            [com.wsscode.pathom.core :as p]
            [glam.models.project :refer [project-resolvers]]
            [glam.models.document :refer [document-resolvers]]
            [glam.models.text-layer :refer [text-layer-resolvers]]
            [glam.models.text :refer [text-resolvers]]
            [glam.models.token-layer :refer [token-layer-resolvers]]
            [glam.models.token :refer [token-resolvers]]
            [glam.models.span-layer :refer [span-layer-resolvers]]
            [glam.models.span :refer [span-resolvers]]
            [glam.models.session :as session]
            [glam.models.user :as user]
            [glam.models.common :refer [server-error]]
            [glam.server.config :refer [config]]
            [glam.server.crux :refer [crux-node]]
            [com.wsscode.pathom.viz.ws-connector.core :as pathom-viz]
            [taoensso.timbre :as log]
            [clojure.walk :as walk]
            [clojure.core.async :as async]))

;; Helpers ================================================================================
(defn mk-augment-env-request
  [get-config-map]
  (fn augment-env-request
    [env]
    (merge env (get-config-map env))))

(defn remove-omissions
  "Replaces black-listed keys from tx with :redacted, meant for logging tx's
  without logging sensitive details like passwords."
  [sensitive-keys tx]
  (walk/postwalk
    (fn [x]
      (cond (and (vector? x) (= 2 (count x)) (sensitive-keys (first x)))
            [(first x) :redacted]
            :else
            x))
    tx))

(defn- log! [msg value]
  (binding [*print-level* 8 *print-length* 4]
    (log/info msg
              (try
                (pr-str value)
                (catch Throwable e
                  (log/error (.getMessage e))
                  "<failed to serialize>")))))

(defn- make-sensitive-keys [parser-config]
  (into #{:com.wsscode.pathom/trace}
        (get parser-config :sensitive-keys #{})))

(defn log-request! [parser-config {:keys [env tx] :as req}]
  (let [sensitive-keys (make-sensitive-keys parser-config)]
    (log! "Transaction: " (remove-omissions sensitive-keys tx))
    {:env env :tx tx}))

(defn log-response!
  [parser-config response]
  (let [sensitive-keys (make-sensitive-keys parser-config)]
    (log! "Response: " (remove-omissions sensitive-keys response))
    response))

;; Parser ================================================================================
(def all-resolvers
  [session/resolvers
   user/resolvers
   project-resolvers
   document-resolvers
   text-layer-resolvers
   text-resolvers
   token-layer-resolvers
   token-resolvers
   span-layer-resolvers
   span-resolvers])

(def env-additions
  (fn [env]
    {:crux         crux-node
     :config       config
     :current-user (user/get-current-user (assoc env :crux crux-node))}))

(defn has-mutation? [tx]
  (and (vector? tx)
       (some #(and (seq? %) (symbol? (first %))) tx)))

(defn make-parser []
  (let [{:keys [trace?
                fail-fast?
                log-requests?
                log-responses?
                connect-viz?
                handle-errors?]} (::config config)
        plugins (cond-> [(pc/connect-plugin {::pc/register all-resolvers})
                         (p/post-process-parser-plugin p/elide-not-found)]

                        log-requests? (conj (p/pre-process-parser-plugin (partial log-request! (::config config))))
                        log-responses? (conj (p/post-process-parser-plugin (partial log-response! (::config config))))
                        env-additions (conj (p/env-wrap-plugin (mk-augment-env-request env-additions)))
                        trace? (conj p/trace-plugin)
                        handle-errors? (conj p/error-handler-plugin))
        parser (cond->> (p/parser
                          {::p/fail-fast? fail-fast?
                           ::p/mutate     pc/mutate
                           ::p/env        {::p/reader                 [p/map-reader
                                                                       pc/reader2
                                                                       pc/index-reader
                                                                       pc/open-ident-reader
                                                                       p/env-placeholder-reader]
                                           ::p/placeholder-prefixes   #{">"}
                                           ::pc/mutation-join-globals [:tempids]}
                           ::p/plugins    plugins})
                        connect-viz? (pathom-viz/connect-parser {::pathom-viz/parser-id ::parser}))
        ;; Ensure that writes happen one at a time GLOBALLY. This avoids certain race conditions.
        ;; In the future, this can be fixed with more heavily relying on crux.tx/matching more, but
        ;; doing things this way is cheaper for the moment.
        ;; Thanks to Souenzzo for this implementation.
        serial-parser (let [in (async/chan)]
                        (async/thread
                          (loop []
                            (when-let [{:keys [env tx out]} (async/<!! in)]
                              (async/>!! out (parser env tx))
                              (recur))))
                        in)]

    (fn wrapped-parser [env tx]
      (when-not (vector? tx) (throw (Exception. "You must pass a vector for the transaction.")))
      ;; Add trace - pathom-viz already adds it so only add if that's not included.
      (let [tx (if (and trace? (not connect-viz?))
                 (conj tx :com.wsscode.pathom/trace) tx)
            use-serial-parser? (has-mutation? tx)
            _ (log/debug (if use-serial-parser? "Pathom tx has a mutation--using global write lock"
                                                "Pathom tx only has reads--performing concurrently"))
            result (if use-serial-parser?
                     (let [out (async/chan)]
                       (async/>!! serial-parser {:env env :tx tx :out out})
                       (let [resp (async/<!! out)]
                         resp))
                     (parser env tx))]
        result))))
