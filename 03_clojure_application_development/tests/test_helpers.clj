(ns test-helpers
  (:require [environ.core :refer [env]]
            [db.migration :refer [migrate-up! migrate-down!]]
            [db.seed :refer [insert-all-seeds!]]))

(defn with-database-reset
  [run-tests]
  ;; Sanity check that we don't accidentally run migrations on our dev
  ;; database when trying to run tests from the REPL
  (when (not= (:environment env) "test")
    (throw (ex-info (str "Cannot reset database for testing in "
                         (:environment env)
                         " environment.")
                    {:type :wrong-environment
                     :expected "test"
                     :actual (:environment env)})))
  ;; with-out-str captures printed output into a string instead of
  ;; printing it to the console. This prevents seeing the
  ;; Ragtime "Applying..." message again and again when migrating
  ;; the database up and down for testing
  (with-out-str
    (migrate-down!)
    (migrate-up!))
  (insert-all-seeds!)
  (run-tests))
