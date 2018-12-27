(ns com.shortify.api.db.core
  (:refer-clojure :exclude [update])
  (:import [org.postgresql.util PSQLException])
  (:require [clojure.string :as string]
            [clojure.java.jdbc :as jdbc]
            [com.stuartsierra.component :as component]
            [ragtime.repl :as ragtime]
            [ragtime.jdbc]
            [clj-time.core :as time]
            [clj-time.coerce :as time-coerce]))

(defn- get-migration-config
  [db]
  (let [{:keys [connection]} db]
    {:datastore (ragtime.jdbc/sql-database connection)
     :migrations (ragtime.jdbc/load-resources "migrations")}))

(defn- get-migration-count
  [db]
  (let [{:keys [connection]} db
        query ["SELECT COUNT(*) FROM ragtime_migrations"]]
    (-> (jdbc/query connection query)
        first
        :count)))

(defprotocol IDatabase
  (create-migration! [this name])
  (migrate-up! [this])
  (rollback! [this])
  (migrate-down! [this]))

(defrecord DB [dbname user password host port]
  component/Lifecycle
  (start [this]
    (let [conn {:dbtype "postgresql"
                :dbname dbname
                :host host
                :port port
                :user user
                :password password}]
      (assoc this :connection conn)))

  (stop [this]
    (assoc this :connection nil))

  IDatabase
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
  (map->DB {:dbname (:database-name env)
            :user (:database-username env)
            :password (:database-password env)
            :host (:database-host env)
            :port (:database-port env)}))
