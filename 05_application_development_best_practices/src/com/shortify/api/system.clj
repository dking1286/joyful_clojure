(ns com.shortify.api.system
  (:require [clojure.spec.alpha :as s]
            [com.stuartsierra.component :as component]
            [com.shortify.api.db.core :refer [db]]
            [com.shortify.api.urls :refer [urls-service]]))

(s/def ::env
  (s/keys :req-un [::database-name
                   ::database-host
                   ::database-port
                   ::database-username
                   ::database-password]))

(defn system
  [env]
  {:pre [(s/valid? ::env env)]}
  (component/system-map
   :db (db env)
   :urls-service (urls-service)))
