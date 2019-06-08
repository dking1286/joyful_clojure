(ns com.shortify.api.urls.core
  (:require [clojure.spec.alpha :as s]
            [clojure.java.jdbc :as jdbc]
            [integrant.core :as ig]
            [com.shortify.api.db.core]))

(defn- random-uuid
  "Generates a random uuid string."
  []
  (.toString (java.util.UUID/randomUUID)))

(defprotocol IUrls
  (get-url [this id] "Retrieves a URL recordyb id.")
  (create-url [this data] "Creates a new URL record."))

(defrecord Urls [db]
  IUrls
  (get-url [this id]
    (let [query ["SELECT * FROM urls WHERE id = ?" id]
          result (jdbc/query db query)
          url (first result)]
      url))

  (create-url [this data]
    (let [{:keys [id url]} data
          id (or id (random-uuid))
          row {:url url :id id}
          result (jdbc/insert! db :urls row)]
      (first result))))

(defmethod ig/init-key :urls
  [_ {:keys [factory config]}]
  (factory config))
