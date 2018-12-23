(ns com.shortify.api.db.core
  (:refer-clojure :exclude [update])
  (:import [org.postgresql.util PSQLException])
  (:require [clojure.string :as string]
            [com.stuartsierra.component :as component]
            [korma.db :refer [create-db default-connection postgres]]
            [korma.core :refer :all]
            [ragtime.repl :as ragtime]
            [ragtime.jdbc :as jdbc]
            [clj-time.core :as time]
            [clj-time.coerce :as time-coerce]))

(defn- get-migration-config
  [db]
  (let [{:keys [spec]} db]
    {:datastore (jdbc/sql-database spec)
     :migrations (jdbc/load-resources "migrations")}))

(defn- get-migration-count
  [db]
  (let [{:keys [migrations]} db]
    (try
      (-> (select migrations (aggregate (count :id) :count))
          first
          :count)
      (catch PSQLException e
        (if (string/includes? (.getMessage e)
                              "\"ragtime_migrations\" does not exist")
          0
          (throw e))))))

(defprotocol IDatabase
  (connect-to-entity [this entity])
  (create-migration! [this name])
  (migrate-up! [this])
  (rollback! [this])
  (migrate-down! [this])
  (seed-all! [this]))

(defrecord DB [db-name user password host port]
  component/Lifecycle
  (start [this]
    (let [spec (postgres {:db db-name
                          :user user
                          :password password
                          :host host
                          :port port})
          connection (create-db spec)
          migrations (-> (create-entity "ragtime_migrations")
                         (table :ragtime_migrations)
                         (database connection))]
      (-> this
          (assoc :spec spec)
          (assoc :connection connection)
          (assoc :migrations migrations))))

  (stop [this]
    (-> this
        (assoc :spec nil)
        (assoc :connection nil)
        (assoc :migrations nil)))

  IDatabase
  (connect-to-entity [this entity]
    (database entity (:connection this))
    entity)

  (create-migration! [_ name]
    (let [timestamp (time-coerce/to-long (time/now))
          up-name (str "resources/migrations/" timestamp "-"
                       name ".up.sql")
          down-name (str "resources/migrations/" timestamp "-"
                         name ".down.sql")]
      (spit up-name "")
      (spit down-name "")))

  (migrate-up! [this]
    (ragtime/migrate (get-migration-config this)))

  (rollback! [this]
    (ragtime/rollback (get-migration-config this)))

  (migrate-down! [this]
    (let [num-migrations (get-migration-count this)]
      (dotimes [_ num-migrations]
        (rollback! this)))))

(defn db
  [env]
  (map->DB {:db-name (:database-name env)
            :user (:database-username env)
            :password (:database-password env)
            :host (:database-host env)
            :port (:database-port env)}))
