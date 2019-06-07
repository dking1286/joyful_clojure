(ns com.shortify.api.db.seed
  (:require [clojure.spec.alpha :as s]
            [clojure.java.io :as io]
            [clojure.java.jdbc :as jdbc]
            [clojure.edn :as edn]
            [com.shortify.api.db.core]))

(s/def ::table keyword?)
(s/def ::data (s/coll-of map?))

(s/def ::seed (s/coll-of (s/keys :req-un [::table ::data])))

(s/fdef insert-seed!
        :args (s/cat :db :com.shortify.api.db.core/db
                     :seed ::seed))

(defn insert-seed!
  "Inserts a single seed definition into the database."
  [db seed]
  (doseq [{:keys [table data]} seed]
    (jdbc/insert-multi! db table data)))

(s/fdef insert-all-seeds!
        :args (s/cat :db :com.shortify.api.db.core/db))

(defn insert-all-seeds!
  "Reads all files in the seeds directory and inserts their contents into
   the database."
  [db]
  (->> (.listFiles (io/file (io/resource "seeds")))
       (map slurp)
       (map edn/read-string)
       (map #(insert-seed! db %))
       doall))
