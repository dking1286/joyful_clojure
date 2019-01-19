(ns com.shortify.client.server.system
  (:require [com.stuartsierra.component :as component]
            [com.shortify.client.server.app :refer [app]]))

(defn system
  [env]
  (component/system-map
   :app (app {:port (Integer/parseInt (:server-port env))})))
