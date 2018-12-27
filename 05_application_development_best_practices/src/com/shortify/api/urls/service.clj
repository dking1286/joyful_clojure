(ns com.shortify.api.urls.service
  (:require [com.stuartsierra.component :as component]
            [com.shortify.api.db.core :as db]))

(defn- create-random-uuid
  []
  (.toString (java.util.UUID/randomUUID)))

(defprotocol IUrlsService
  (create-url [this data])
  (get-url [this id]))

(defrecord UrlsService [db]
  component/Lifecycle
  (start [this] this)
  (stop [this] this)

  IUrlsService
  (create-url [this data]
    (let [{:keys [db]} this
          id (or (:id data) (create-random-uuid))
          values (merge {:id id} data)]
      (db/insert! db :urls values)
      (get-url this id)))

  (get-url [this id]
    (let [{:keys [db]} this
          query ["SELECT * FROM urls WHERE id = ?" id]]
      (first (db/query db query)))))

(defn urls-service
  []
  (map->UrlsService {}))
