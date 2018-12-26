(ns com.shortify.api.system
  (:require [clojure.spec.alpha :as s]
            [com.stuartsierra.component :as component]
            [com.shortify.api.db.core :refer [db]]
            [com.shortify.api.urls.service :refer [urls-service]]
            [com.shortify.api.db.seed :refer [db-seeder]]))

(defn component?
  [thing]
  (satisfies? component/Lifecycle thing))

(s/def ::env (s/keys :req-un [::database-name
                              ::database-host
                              ::database-port
                              ::database-username
                              ::database-password]))

(s/def ::component-name #{:db
                          :urls-service
                          :db-seeder})

(s/def ::component component?)

(s/def ::system (s/map-of ::component-name ::component))

(defn system
  [env]
  {:pre [(s/valid? ::env env)]
   :post [(s/valid? ::system %)]}
  (component/system-map
   :db (db env)
   :urls-service (component/using (urls-service)
                                  [:db])
   :db-seeder (component/using (db-seeder)
                               [:urls-service])))
