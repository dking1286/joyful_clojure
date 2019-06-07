(ns com.shortify.api.db.migration
  (:import [org.postgresql.util PSQLException])
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [clojure.java.jdbc :as jdbc]
            [ragtime.jdbc]
            [ragtime.repl :as ragtime]
            [clj-time.core :as time]
            [clj-time.coerce :as time-coerce]
            [com.shortify.api.db.core :as db-core]))

(defn- get-migration-config
  "Constructs the configuration map needed by Ragtime to run migrations
   on the database."
  [db]
  {:datastore (ragtime.jdbc/sql-database db)
   :migrations (ragtime.jdbc/load-resources "migrations")})

(defn- get-migration-count
  "Queries the database to determine how many migrations have been
   previously run."
  [db]
  (try
    (let [query ["SELECT COUNT(id) AS count FROM ragtime_migrations"]
          result (jdbc/query db query)]
      (:count (first result)))
    (catch PSQLException e
      (if (string/includes? (.getMessage e)
                            "\"ragtime_migrations\" does not exist")
        0
        (throw e)))))

(s/fdef create-migration!
        :args (s/cat :name string?))

(defn create-migration!
  "Creates skeleton migration files in the migrations directory,
   labeling the files with the current timestamp."
  [name]
  (let [timestamp (time-coerce/to-long (time/now))
        up-name (str "resources/migrations/" timestamp "-"
                     name ".up.sql")
        down-name (str "resources/migrations/" timestamp "-"
                       name ".down.sql")]
    (spit up-name "")
    (spit down-name "")))

(s/fdef migrate-up!
        :args (s/cat :db :com.shortify.api.db.core/db))

(defn migrate-up!
  "Runs all pending migrations on the database."
  [db]
  (ragtime/migrate (get-migration-config db)))

(s/fdef rollback!
        :args (s/cat :db :com.shortify.api.db.core/db))

(defn rollback!
  "Rolls back the most recent migration on the database."
  [db]
  (ragtime/rollback (get-migration-config db)))

(s/fdef migrate-down!
        :args (s/cat :db :com.shortify.api.db.core/db))

(defn migrate-down!
  "Rolls back all migrations on the database. Use with caution, this
   will delete all existing data."
  [db]
  (let [num-migrations (get-migration-count db)]
    (dotimes [_ num-migrations]
      (rollback! db))))
