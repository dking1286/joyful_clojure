(ns com.shortify.api.urls
  (:refer-clojure :exclude [update])
  (:require [com.stuartsierra.component :as component]
            [korma.core :refer :all]
            [korma.db :refer [transaction]]
            [com.shortify.api.db.core :refer [connect-to-entity]]))

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
      (connect-to-entity db entity)
      (-> this
          (assoc :entity entity))))

  (stop [this]
    (-> this
        (assoc :entity nil)))

  IUrlsService
  (create-url [this data]
    (let [id (create-random-uuid)
          url (merge data {:id id})]
      (transaction
       (insert (:entity this) (values url)))
      (get-url this id)))

  (get-url [this id]
    (let [query-result (select (:entity this) (where (= :id id)))]
      (first query-result))))

(defn urls-service
  []
  (component/using (map->UrlsService {})
                   [:db]))
