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
  []
  {:datastore (ragtime.jdbc/sql-database connection)
   :migrations (ragtime.jdbc/load-resources "migrations")})

(defn get-migration-count
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
  [name]
  (let [timestamp (time-coerce/to-long (time/now))
        up-name (str "resources/migrations/" timestamp "-"
                     name ".up.sql")
        down-name (str "resources/migrations/" timestamp "-"
                       name ".down.sql")]
    (spit up-name "")
    (spit down-name "")))

(defn migrate-up!
  []
  (ragtime/migrate (get-migration-config)))

(defn rollback!
  []
  (ragtime/rollback (get-migration-config)))

(defn migrate-down!
  []
  (let [num-migrations (get-migration-count)]
    (doseq [_ (range num-migrations)]
      (rollback!))))
