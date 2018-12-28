(ns com.shortify.api.system
  (:require [clojure.spec.alpha :as s]
            [com.stuartsierra.component :as component]
            [com.shortify.api.db.core :refer [db transient-db]]
            [com.shortify.api.urls.service :refer [urls-service]]
            [com.shortify.api.urls.handler :refer [urls-handler]]
            [com.shortify.api.db.seed :refer [db-seeder]]
            [com.shortify.api.routes :refer [routes]]
            [com.shortify.api.handler :refer [handler]]
            [com.shortify.api.server :refer [jetty-server]]))

(defn component?
  [thing]
  (satisfies? component/Lifecycle thing))

(s/def ::env (s/keys :req-un [::database-name
                              ::database-host
                              ::database-port
                              ::database-username
                              ::database-password
                              ::database-seed-resource-path
                              ::server-port]))

(s/def ::component-name #{:db
                          :urls-service
                          :urls-handler
                          :db-seeder
                          :routes
                          :handler
                          :server})

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
   :urls-handler (component/using (urls-handler)
                                  [:urls-service])
   :db-seeder (component/using (db-seeder env)
                               [:urls-service])
   :routes (component/using (routes)
                            [:urls-handler])
   :handler (component/using (handler)
                             [:routes])
   :server (component/using (jetty-server env)
                            [:handler])))

(defn test-system
  [env]
  {:pre [(s/valid? ::env env)]
   :post [(s/valid? ::system %)]}
  (assoc (system env)
         :db (transient-db env)))
