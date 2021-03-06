(ns user
  (:require
    [clojure.spec.alpha :as s]
    [clojure.tools.namespace.repl :as tools-ns]
    [expound.alpha :as expound]
    [mount.core :as mount]
    [shadow.cljs.devtools.api :as shadow]
    ;; this is the top-level dependent component...mount will find the rest via ns requires
    [glam.server.http-server :refer [http-server]]
    [glam.crux.easy :as gce]
    [glam.crux.user :as usr]
    [glam.crux.project :as prj]
    [glam.crux.text-layer :as txtl]
    [glam.crux.token-layer :as tokl]
    [glam.crux.span-layer :as sl]
    ))

;; ==================== SERVER ====================
(tools-ns/set-refresh-dirs "src/main" "src/dev" "src/test")

;; Change the default output of spec to be more readable
(alter-var-root #'s/*explain-out* (constantly expound/printer))

(comment
  ;; For shadow: start the shadow-cljs server using ./scripts/start-dev.sh
  ;; start a cljs repl once shadow is listening (port 9000, default)
  ;; then you can start the watch processes here (ensure the cljs repl has focus in cursive):
  ;;
  (shadow/repl :main)
  (shadow/help)

  ;; You have to start the node server first.
  ;; in terminal, run: node target/node-server.js
  (shadow/repl :node-server)
  ;; now you can send cljs forms to the repl but you'll need to open another cljs repl
  ;; as the server has captured output.
  )

(defn start "Start the web server + services" []
  (mount/start)
  (def node glam.server.crux/crux-node))

(defn stop "Stop the web server + services" []
  (mount/stop))

(defn restart
  "Stop, reload code, and restart the server. If there is a compile error, use:
  ```
  (tools-ns/refresh)
  ```
  to recompile, and then use `start` once things are good."
  []
  (stop)
  (tools-ns/refresh :after 'user/start))

(comment
  (tools-ns/refresh :after 'user/start)
  (shadow/repl :main)
  (stop)
  (restart))

;; crux stuff
(defn init-db [crux-node]
  (let [node (or crux-node glam.server.crux/crux-node)]
    (let [admin-id (:id (usr/create
                          node
                          {:user/password-hash "100$12$argon2id$v13$u6JYj16Ize35J1uuTN6KwQ$SblXBBHdyMZ5K52RwCcO41/SNL6XqoY1JBouP/V01uQ$$$"
                           :user/name          "admin"
                           :user/email         "a@b.com"
                           :user/id            :admin}))
          user1 (:id (usr/create
                       node
                       {:user/password-hash "100$12$argon2id$v13$u6JYj16Ize35J1uuTN6KwQ$SblXBBHdyMZ5K52RwCcO41/SNL6XqoY1JBouP/V01uQ$$$"
                        :user/name          "user"
                        :user/email         "b@b.com"
                        :user/id            :user1}))
          user2 (:id (usr/create
                       node
                       {:user/password-hash "100$12$argon2id$v13$u6JYj16Ize35J1uuTN6KwQ$SblXBBHdyMZ5K52RwCcO41/SNL6XqoY1JBouP/V01uQ$$$"
                        :user/name          "user2"
                        :user/email         "c@c.com"
                        :user/id            :user2}))

          project1 (:id (prj/create node {:project/name "Project 1" :project/id :project1}))
          project2 (:id (prj/create node {:project/name "Project 2" :project/id :project2}))
          project3 (:id (prj/create node {:project/name "Project 3" :project/id :project3}))
          project4 (:id (prj/create node {:project/name "Project 4" :project/id :project4}))

          txtl1 (:id (txtl/create node {:text-layer/name "Layer 1" :text-layer/id :txtl1}))
          tokl1 (:id (tokl/create node {:token-layer/name "Token Layer 1" :token-layer/id :tokl1}))
          sl1 (:id (sl/create node {:span-layer/name "Span Layer 1" :span-layer/id :sl1}))
          ]

      (prj/add-writer node project1 admin-id)
      (prj/add-writer node project2 admin-id)

      (prj/add-writer node project1 user1)
      (prj/add-reader node project2 user1)
      (prj/add-reader node project1 user1)

      (prj/add-writer node project2 user2)
      (prj/add-writer node project3 user2)

      (prj/add-text-layer node project1 txtl1)
      (txtl/add-token-layer node txtl1 tokl1)
      (tokl/add-span-layer node tokl1 sl1)
      )))

