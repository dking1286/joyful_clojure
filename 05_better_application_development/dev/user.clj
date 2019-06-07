(ns user
  (:require [integrant.core :as ig]
            [orchestra.spec.test :as orchestra]
            [clojure.tools.namespace.repl :as repl]
            [com.shortify.api.system :as sys]
            [com.shortify.api.db.migration :as migration]
            [com.shortify.api.db.seed :as seed]))

(def dev-config
  (merge sys/config {:instrumentation {}}))

(defmethod ig/init-key :instrumentation
  [_ _]
  (orchestra/instrument))

(defmethod ig/halt-key! :instrumentation
  [_ _]
  (orchestra/unstrument))

(def system nil)

(defn start
  "Creates and starts a new system, and assigns it as the value of the
  'system' var."
  []
  (println "Starting system...")
  (let [new-system (ig/init dev-config)]
    (alter-var-root #'system (constantly new-system))))

(defn stop
  []
  "Stops and discards the running system, if one exists. Otherwise does
  nothing."
  (when system
    (println "Stopping system...")
    (ig/halt! system)
    (alter-var-root (var system) (constantly nil))))

(defn reset
  "Stops the running system, refreshes all namespaces, and starts a new
  system."
  []
  (stop)
  (repl/refresh :after 'user/start))

(defn migrate-up!
  []
  (let [{:keys [db]} system]
    (migration/migrate-up! db)))

(defn rollback!
  []
  (let [{:keys [db]} system]
    (migration/rollback! db)))

(defn migrate-down!
  []
  (let [{:keys [db]} system]
    (migration/migrate-down! db)))

(defn insert-all-seeds!
  []
  (let [{:keys [db]} system]
    (seed/insert-all-seeds! db)))
