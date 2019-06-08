(ns com.shortify.api.test-helpers
  (:import [java.io StringReader])
  (:require [clojure.data.json :as json]
            [integrant.core :as ig]
            [com.shortify.api.db.migration :as migration]
            [com.shortify.api.db.seed :as seed]
            [com.shortify.api.system :as sys]))

(defn- to-reader
  [body]
  (StringReader. (json/write-str body)))

(defn- serialize
  [req]
  (-> req
      (assoc-in [:headers "content-type"] "application/json")
      (update :body to-reader)))

(defn- parse
  [res]
  (try
    (update res :body #(json/read-str % :key-fn keyword))
    (catch Exception e
      res)))

(defn request
  [app req]
  (parse (app (serialize req))))

(def ^:dynamic *system*)

(def test-config
  (-> sys/config
      ;; Use the test database
      (assoc-in [:db :dbname] "url_shortening_db_test")
      ;; Disable request logging in integration tests
      (assoc-in [:app :logging?] false)
      ;; Do not start a server instance in tests,
      ;; b/c we can call the handler directly.
      (dissoc :server)))

(defn with-test-system
  [run-tests]
  (let [{:keys [db] :as system} (ig/init test-config)]
    (with-out-str
      (migration/migrate-down! db)
      (migration/migrate-up! db))
    (seed/insert-all-seeds! db)
    (binding [*system* system]
      (run-tests))
    (ig/halt! system)))
