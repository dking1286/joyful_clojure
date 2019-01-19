(ns user
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [com.stuartsierra.component :as component]
            [environ.core :refer [env]]
            [com.shortify.client.build :as build :refer [cljs-repl]]
            [com.shortify.client.server.system :as server-sys]))

;; Server system management functions

(defonce system nil)

(defn init
  []
  (println "Initializing system...")
  (alter-var-root #'system
                  (constantly (server-sys/system env))))

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

;; Development build system management functions

(defonce build-system nil)

(defn init-build
  []
  (println "Initializing builds...")
  (alter-var-root #'build-system
                  (constantly (build/dev-build-system))))

(defn start-build
  []
  (println "Starting builds...")
  (alter-var-root #'build-system component/start))

(defn stop-build
  []
  (println "Stopping build...")
  (alter-var-root #'build-system component/stop))

(defn build
  []
  (init-build)
  (start-build))
