(ns com.shortify.api.server
  (:require [com.stuartsierra.component :as component]
            [ring.adapter.jetty :refer [run-jetty]]
            [com.shortify.api.handler :refer [get-handler]]))

(defrecord JettyServer [handler port]
  component/Lifecycle
  (start [this]
    (let [server-promise (promise)
          handler-fn (get-handler handler)
          on-server-start (fn [server]
                            (deliver server-promise server))
          jetty-opts {:port (Integer/parseInt port)
                      :join? false
                      :configurator on-server-start}]
      (run-jetty handler-fn jetty-opts)
      (println (str "Server litening on port " port "..."))
      (assoc this :server-instance @server-promise)))

  (stop [this]
    (let [{:keys [server-instance]} this]
      (when server-instance
        (.stop server-instance))
      (assoc this :server-instance nil))))

(defn jetty-server
  [env]
  (map->JettyServer {:port (:server-port env)}))
