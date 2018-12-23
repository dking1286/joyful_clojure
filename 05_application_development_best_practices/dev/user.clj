(ns user
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [com.stuartsierra.component :as component]
            [environ.core :refer [env]]
            [com.shortify.api.system]
            [com.shortify.api.db.core :refer [create-migration!
                                              migrate-up!
                                              rollback!
                                              migrate-down!]]))

(defonce system nil)

(defn init
  []
  (println "Initializing system...")
  (alter-var-root #'system
                  (constantly (com.shortify.api.system/system env))))

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
