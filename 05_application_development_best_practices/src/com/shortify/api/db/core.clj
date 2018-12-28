(ns com.shortify.api.db.core
  (:import [org.postgresql.util PSQLException])
  (:require [clojure.string :as string]
            [clojure.java.jdbc :as jdbc]
            [com.stuartsierra.component :as component]
            [ragtime.repl :as ragtime]
            [ragtime.jdbc]
            [clj-time.core :as time]
            [clj-time.coerce :as time-coerce]
            [com.shortify.api.utils.uuid :refer [create-random-uuid]]))

(def ^:private ^:dynamic *connection* nil)

(defprotocol IDatabase
  (query [this q])
  (execute! [this q])
  (insert! [this table values])
  (invoke-in-transaction [this f])
  (create-migration! [this name])
  (migrate-up! [this])
  (rollback! [this])
  (migrate-down! [this]))

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

(defn- create-random-database-name
  []
  (as-> (create-random-uuid) $
        (string/split $ #"-")
        (string/join $)
        (str "temp_database_" $)))

(defn- init-transient-database
  [db]
  (let [{:keys [connection dbname]} db
        temp-connection (assoc connection :dbname "postgres")
        create-database-query [(str "CREATE DATABASE " dbname ";")]]
    (jdbc/execute! temp-connection
                   create-database-query
                   {:transaction? false})
    (migrate-up! db)))

(defn- drop-transient-database
  [db]
  (let [{:keys [connection dbname]} db
        temp-connection (assoc connection :dbname "postgres")
        drop-database-query [(str "DROP DATABASE " dbname ";")]]
    (jdbc/execute! temp-connection
                   drop-database-query
                   {:transaction? false})))

(defrecord DB [dbname user password host port transient?]
  component/Lifecycle
  (start [this]
    (let [connection {:dbtype "postgresql"
                      :dbname dbname
                      :host host
                      :port port
                      :user user
                      :password password}
          component (assoc this :connection connection)]
      (when transient?
        (init-transient-database component))
      component))

  (stop [this]
    (when transient?
      (drop-transient-database this))
    (assoc this :connection nil))

  IDatabase
  (query [this q]
    (try
      (binding [*connection* (or *connection* (:connection this))]
        (jdbc/query *connection* q))
      (catch PSQLException e
        (throw (get-error e)))))

  (execute! [this q]
    (try
      (binding [*connection* (or *connection* (:connection this))]
        (jdbc/execute! *connection* q))
      (catch PSQLException e
        (throw (get-error e)))))

  (insert! [this table values]
    (try
      (binding [*connection* (or *connection* (:connection this))]
        (jdbc/insert! *connection* table values))
      (catch PSQLException e
        (throw (get-error e)))))

  (invoke-in-transaction [this f]
    (jdbc/with-db-transaction [trx (:connection this)]
                              (binding [*connection* trx]
                                (f trx))))

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

(defn transient-db
  [env]
  (map->DB {:dbname (create-random-database-name)
            :user (:database-username env)
            :password (:database-password env)
            :host (:database-host env)
            :port (:database-port env)
            :transient? true}))

(defmacro with-transaction
  [[trx-sym db-sym] & body]
  `(invoke-in-transaction ~db-sym (fn [~trx-sym] ~@body)))
