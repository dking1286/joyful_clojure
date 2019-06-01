(ns db.migration
  (:import [org.postgresql.util PSQLException])
  (:require [clojure.string :as string]
            [clojure.java.jdbc :as jdbc]
            [ragtime.jdbc]
            [ragtime.repl :as ragtime]
            [db.core :refer [connection]]
            [clj-time.core :as time]
            [clj-time.coerce :as time-coerce]))

(defn get-migration-config
  "Constructs the configuration map needed by Ragtime to run migrations
   on the database."
  []
  {:datastore (ragtime.jdbc/sql-database connection)
   :migrations (ragtime.jdbc/load-resources "migrations")})

(defn get-migration-count
  "Queries the database to determine how many migrations have been
   previously run."
  []
  (try
    (let [query ["SELECT COUNT(id) AS count FROM ragtime_migrations"]
          result (jdbc/query connection query)]
      (:count (first result)))
    (catch PSQLException e
      (if (string/includes? (.getMessage e)
                            "\"ragtime_migrations\" does not exist")
        0
        (throw e)))))

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

(defn migrate-up!
  "Runs all pending migrations on the database."
  []
  (ragtime/migrate (get-migration-config)))

(defn rollback!
  "Rolls back the most recent migration on the database."
  []
  (ragtime/rollback (get-migration-config)))

(defn migrate-down!
  "Rolls back all migrations on the database. Use with caution, this
   will delete all existing data."
  []
  (let [num-migrations (get-migration-count)]
    (dotimes [_ num-migrations]
      (rollback!))))
