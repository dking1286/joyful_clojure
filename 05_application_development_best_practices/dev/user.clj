(ns user
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.java.jdbc :as jdbc]
            [com.stuartsierra.component :as component]
            [environ.core :refer [env]]
            [com.shortify.api.system :as sys]
            [com.shortify.api.db.core :as db]
            [com.shortify.api.urls.service :as urls-service]
            [com.shortify.api.db.seed :as seed]))

;; System management functions

(defonce system nil)

(defn init
  []
  (println "Initializing system...")
  (alter-var-root #'system
                  (constantly (sys/system env))))

(defn start
  []
  (println "Starting system...")
  (alter-var-root #'system component/start))

(defn stop
  []
  (when system
    (println "Stopping system...")
    (alter-var-root #'system component/stop)))

(defn go
  []
  (init)
  (start))

(defn reset
  []
  (stop)
  (refresh :after 'user/go))

;; Database management functions

(defn migrate-up!
  []
  (let [database (:db system)]
    (db/migrate-up! database)))

(defn rollback!
  []
  (let [database (:db system)]
    (db/rollback! database)))

(defn migrate-down!
  []
  (let [database (:db system)]
    (db/migrate-down! database)))

;; Database seeding functions

(defn insert-all-seeds!
  []
  (let [db-seeder (:db-seeder system)]
    (seed/insert-all-seeds! db-seeder)))

;; Low level database querying functions

(defn query
  [q]
  (let [{:keys [db]} system]
    (db/query db q)))

(defn execute!
  [q]
  (let [{:keys [db]} system]
    (db/execute! db q)))

;; Urls service

(defn get-url
  [id]
  (let [{:keys [urls-service]} system]
    (urls-service/get-url urls-service id)))

(defn create-url
  [values]
  (let [{:keys [urls-service]} system]
    (urls-service/create-url urls-service values)))
