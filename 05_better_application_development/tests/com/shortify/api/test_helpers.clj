(ns com.shortify.api.test-helpers
  (:import [java.io StringReader])
  (:require [clojure.data.json :as json]
            [integrant.core :as ig]
            [com.shortify.api.db.migration :as migration]
            [com.shortify.api.db.seed :as seed]
            [com.shortify.api.system :as sys]))

(defn- to-reader
  "Converts a Clojure data structure to a Reader containing a JSON
  representation of the data structure."
  [body]
  (StringReader. (json/write-str body)))

(defn- serialize
  "Converts a Clojure data structure in the body of a request into a reader
  containing a JSON representation of the data structure. This is necessary
  because the ring-json middleware expects the request body to be a Reader."
  [req]
  (-> req
      (assoc-in [:headers "content-type"] "application/json")
      (update :body to-reader)))

(defn- parse
  "Converts a JSON string in the body of a response into an equivalent
  Clojure data structure."
  [res]
  (try
    (update res :body #(json/read-str % :key-fn keyword))
    (catch Exception e
      res)))

(defn request
  "Simulates sending a request to the application for testing.

  The request body should be a plain Clojure data structure. A response map
  with a plain Clojure data structure in the body is returned."
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

(defn- reset-database!
  "Resets the database to a known state for testing."
  [db]
  (with-out-str
    (migration/migrate-down! db)
    (migration/migrate-up! db))
  (seed/insert-all-seeds! db))

(defn with-test-system
  "Test fixture to set up and tear down the test system.

  The test is run with the test system bound to the *system* dynamic var"
  [run-tests]
  (let [{:keys [db] :as system} (ig/init test-config)]
    (reset-database! db)
    (binding [*system* system]
      (run-tests))
    (ig/halt! system)))
