(ns db.migration
  (:refer-clojure :exclude [update])
  (:import [org.postgresql.util PSQLException])
  (:require [clojure.string :as string]
            [ragtime.jdbc :as jdbc]
            [ragtime.repl :as ragtime]
            [korma.core :refer :all]
            [db.core :refer [db connection]]
            [clj-time.core :as time]
            [clj-time.coerce :as time-coerce]))

(defentity ragtime-migrations
  (table :ragtime_migrations)
  (database db))

(defn get-migration-config
  []
  {:datastore (jdbc/sql-database connection)
   :migrations (jdbc/load-resources "migrations")})

(defn get-migration-count
  []
  (try
    (-> (select ragtime-migrations (aggregate (count :id) :count))
        first
        :count)
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
