(ns test-helpers
  (:require [environ.core :refer [env]]
            [db.migration :refer [migrate-up! migrate-down!]]
            [db.seed :refer [insert-all-seeds!]]))

(defn with-database-reset
  [run-tests]
  (when (not= (:environment env) "test")
    (throw (ex-info (str "Cannot reset database for testing in "
                         (:environment env)
                         " environment.")
                    {:type :wrong-environment
                     :expected "test"
                     :actual (:environment env)})))
  (migrate-down!)
  (migrate-up!)
  (insert-all-seeds!)
  (run-tests))
