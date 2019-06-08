(ns com.shortify.api.server
  (:require [integrant.core :as ig]
            [environ.core :refer [env]]
            [ring.adapter.jetty :as jetty]))

(defn- env-config
  "Gets the server configuration options from the env map."
  []
  {:port (Integer/parseUnsignedInt (:port env))})

(defn- start-server
  "Starts a Jetty server with the provided handler and returns the running
  server instance. Pulls Jetty configuration options from the 'config' argument
  and the env map, with 'config' taking precedence."
  [app config]
  (let [server-promise (promise)
        jetty-opts {;; Do not block the current thread waiting for the server to
                    ;; finish
                    :join? false
                    ;; Callback function invoked with the running server instance
                    ;; once the server has started
                    :configurator #(deliver server-promise %)}
        full-config (merge jetty-opts (env-config) config)]
    (jetty/run-jetty app full-config)
    @server-promise))

(defmethod ig/init-key :server
  [_ {:keys [app config]}]
  (start-server app config))

(defmethod ig/halt-key! :server
  [_ server]
  (.stop server))
