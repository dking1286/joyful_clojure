(ns com.shortify.api.server
  (:require [integrant.core :as ig]
            [environ.core :refer [env]]
            [ring.adapter.jetty :as jetty]))

(defn- env-config
  []
  {:port (Integer/parseUnsignedInt (:port env))})

(defmethod ig/init-key :server
  [_ {:keys [app config]}]
  (let [server-promise (promise)
        jetty-opts {:join? false
                    :configurator #(deliver server-promise %)}
        full-config (merge jetty-opts (env-config) config)]
    (jetty/run-jetty app full-config)
    @server-promise))

(defmethod ig/halt-key! :server
  [_ server]
  (.stop server))
