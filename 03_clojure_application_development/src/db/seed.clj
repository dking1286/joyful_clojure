(ns db.seed
  (:import [org.postgresql.util PSQLException])
  (:require [clojure.java.io :as io]
            [clojure.java.jdbc :as jdbc]
            [clojure.edn :as edn]
            [db.core :refer [connection]]))

(defn insert-seed!
  "Inserts a single seed definition into the database."
  [seed]
  (doseq [{:keys [table data]} seed]
    (jdbc/insert-multi! connection table data)))

(defn insert-all-seeds!
  "Reads all files in the seeds directory and inserts their contents into
   the database."
  []
  (->> (.listFiles (io/file (io/resource "seeds")))
       (map slurp)
       (map edn/read-string)
       (map insert-seed!)
       doall))
