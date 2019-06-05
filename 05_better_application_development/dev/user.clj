(ns user
  (:require [integrant.core :as ig]
            [clojure.tools.namespace.repl :as ctnr]
            [com.shortify.system :as sys]
            [com.shortify.db.migration :as migration]
            [com.shortify.db.seed :as seed]))

(def system nil)

(defn start
  []
  (println "Starting system...")
  (let [new-system (ig/init sys/config)]
    (alter-var-root (var system) (constantly new-system))))

(defn stop
  []
  (when system
    (println "Stopping system...")
    (ig/halt! system)
    (alter-var-root (var system) (constantly nil))))

(defn reset
  []
  (stop)
  (ctnr/refresh :after 'user/start))
