(ns com.shortify.api.db.core
  (:import [org.postgresql.util PSQLException])
  (:require [clojure.string :as string]
            [clojure.java.jdbc :as jdbc]
            [com.stuartsierra.component :as component]
            [ragtime.repl :as ragtime]
            [ragtime.jdbc]
            [clj-time.core :as time]
            [clj-time.coerce :as time-coerce]))

(defn- conflict-error
  [e]
  (ex-info (.getMessage e) {:type :conflict-error}))

(defn get-error
  [e]
  (let [message (.getMessage e)]
    (cond
      (string/includes? message
                        "duplicate key value violates unique constraint")
      (conflict-error e)

      :else e)))

(defn- get-migration-config
  [db]
  (let [{:keys [connection]} db]
    {:datastore (ragtime.jdbc/sql-database connection)
     :migrations (ragtime.jdbc/load-resources "migrations")}))

(defn- get-migration-count
  [db]
  (try
    (let [{:keys [connection]} db
          query ["SELECT COUNT(*) FROM ragtime_migrations"]
          result (jdbc/query connection query)]
      (:count (first result)))
    (catch PSQLException e
      (if (string/includes? (.getMessage e)
                            "ragtime_migrations")
        0
        (throw e)))))

(defprotocol IDatabase
  (query [this q])
  (execute! [this q])
  (insert! [this table values])
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
  (query [this q]
    (try
      (jdbc/query (:connection this) q)
      (catch PSQLException e
        (throw (get-error e)))))

  (execute! [this q]
    (try
      (jdbc/execute! (:connection this) q)
      (catch PSQLException e
        (throw (get-error e)))))

  (insert! [this table values]
    (try
      (jdbc/insert! (:connection this) table values)
      (catch PSQLException e
        (throw (get-error e)))))

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
