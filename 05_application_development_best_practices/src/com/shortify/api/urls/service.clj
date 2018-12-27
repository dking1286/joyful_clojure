(ns com.shortify.api.urls.service
  (:refer-clojure :exclude [update])
  (:require [com.stuartsierra.component :as component]))

(defn- create-random-uuid
  []
  (.toString (java.util.UUID/randomUUID)))

(defprotocol IUrlsService
  (create-url [this data])
  (get-url [this id]))

(defrecord UrlsService [db]
  component/Lifecycle
  (start [this])

  (stop [this])

  IUrlsService
  (create-url [this data])

  (get-url [this id]))

(defn urls-service
  []
  (map->UrlsService {}))
