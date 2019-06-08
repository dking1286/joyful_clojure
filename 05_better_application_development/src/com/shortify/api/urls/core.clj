(ns com.shortify.api.urls.core
  (:require [clojure.spec.alpha :as s]
            [clojure.java.jdbc :as jdbc]
            [integrant.core :as ig]
            [com.shortify.api.db.core]))

(defn- random-uuid
  "Generates a random uuid string."
  []
  (.toString (java.util.UUID/randomUUID)))

(defn get-url
  "Retrieves a URL record by id."
  [db id]
  (let [query ["SELECT * FROM urls WHERE id = ?" id]
        result (jdbc/query db query)]
    (first result)))

(defn create-url
  "Creates a new URL record."
  [db data]
  (let [{:keys [id url]} data
        id (or id (random-uuid))
        row {:url url :id id}
        result (jdbc/insert! db :urls row)]
    (first result)))
