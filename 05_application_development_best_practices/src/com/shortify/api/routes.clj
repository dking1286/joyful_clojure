(ns com.shortify.api.routes
  (:require [com.stuartsierra.component :as component]
            [compojure.core :as compojure :refer [GET POST context]]
            [compojure.route :as route]
            [com.shortify.api.urls.handler :refer [create-url get-url]]))

(defprotocol IRoutes
  (get-routes [this]))

(defrecord Routes [urls-handler]
  IRoutes
  (get-routes [this]
    (compojure/routes
     (GET "/" [] "Hello world")
     (context "/urls" []
              (POST "/" [] (fn [req] (create-url urls-handler req)))
              (GET "/:id" [] (fn [req] (get-url urls-handler req)))))))

(defn routes
  []
  (map->Routes {}))
