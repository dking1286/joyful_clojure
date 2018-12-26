(ns com.shortify.api.urls.service
  (:refer-clojure :exclude [update])
  (:require [com.stuartsierra.component :as component]
            [korma.core :refer :all]
            [com.shortify.api.db.core :refer [connect-to-entity
                                              with-transaction]]))

(defn- create-random-uuid
  []
  (.toString (java.util.UUID/randomUUID)))

(defprotocol IUrlsService
  (create-url [this data])
  (get-url [this id]))

(defrecord UrlsService [db]
  component/Lifecycle
  (start [this]
    (let [entity (create-entity :urls)]
      (-> this
          (assoc :entity (connect-to-entity db entity)))))

  (stop [this]
    (-> this
        (assoc :entity nil)))

  IUrlsService
  (create-url [this data]
    (let [{:keys [db entity]} this
          id (create-random-uuid)
          url (merge {:id id} data)]
      (with-transaction db
       (insert entity (values url)))
      (get-url this id)))

  (get-url [this id]
    (let [query-result (select (:entity this) (where (= :id id)))]
      (first query-result))))

(defn urls-service
  []
  (map->UrlsService {}))
