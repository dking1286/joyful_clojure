(ns com.shortify.db.seed
  (:require [clojure.spec.alpha :as s]
            [clojure.java.io :as io]
            [clojure.java.jdbc :as jdbc]
            [clojure.edn :as edn]
            [com.shortify.db.core :as db-core]
            [com.shortify.utils.spec :as su]))

(s/def ::table keyword?)
(s/def ::data (s/coll-of map?))

(s/def ::seed (s/keys :req-un [::table ::data]))

(defn insert-seed!
  "Inserts a single seed definition into the database."
  [db seed]
  {:pre [(su/valid? :db-core/db db)
         (su/valid? ::seed seed)]}
  (doseq [{:keys [table data]} seed]
    (jdbc/insert-multi! db table data)))

(defn insert-all-seeds!
  "Reads all files in the seeds directory and inserts their contents into
   the database."
  [db]
  {:pre [(su/valid? :db-core/db db)]}
  (->> (.listFiles (io/file (io/resource "seeds")))
       (map slurp)
       (map edn/read-string)
       (map #(insert-seed! db %))
       doall))
