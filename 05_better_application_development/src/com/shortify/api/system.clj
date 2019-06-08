(ns com.shortify.api.system
  (:require [integrant.core :as ig]
            [environ.core :refer [env]]
            [com.shortify.api.urls.core :as urls]
            [com.shortify.api.urls.handler :as urls-handler]))

(def config
  {:db {}
   :app {:db (ig/ref :db)}
   :server {:app (ig/ref :app) :config {}}})
