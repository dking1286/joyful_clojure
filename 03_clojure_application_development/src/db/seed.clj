(ns db.seed
  (:refer-clojure :exclude [update])
  (:import [org.postgresql.util PSQLException])
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [korma.core :refer :all]
            [korma.db :refer [transaction]]))

(defn insert-all-ignore-duplicates!
  [seeds]
  (doseq [seed seeds]
    (let [db-entity (deref (resolve (get seed :entity)))]
      (try
        (insert db-entity (values (get seed :data)))
        (catch PSQLException e
          ;; Do nothing
          )))))

(defn seed-all!
  []
  (let [seed-files (.listFiles (io/file (io/resource "seeds")))]
    (doseq [seed-file seed-files]
      (-> (slurp seed-file)
          edn/read-string
          insert-all-ignore-duplicates!))))
