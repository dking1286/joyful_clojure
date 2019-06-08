(ns com.shortify.api.system
  (:require [integrant.core :as ig]
            [environ.core :refer [env]]
            [com.shortify.api.urls.core :as urls]
            [com.shortify.api.urls.handler :as urls-handler]))

(def config
  {:db
   {}

   :urls
   {:factory urls/map->Urls
    :config {:db (ig/ref :db)}}

   :urls-handler
   {:factory urls-handler/map->UrlsHandler
    :config {:urls (ig/ref :urls)}}

   :app
   {:urls-handler (ig/ref :urls-handler)}

   :server
   {:app (ig/ref :app) :config {}}})
