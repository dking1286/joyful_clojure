(ns com.shortify.api.app
  (:require [integrant.core :as ig]
            [ring.middleware.json :refer [wrap-json-body
                                          wrap-json-response]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [com.shortify.api.middleware.logging :refer [wrap-logging]]
            [com.shortify.api.middleware.error-handling :refer [wrap-error-handling]]
            [com.shortify.api.urls.handler :as uh]))

(defn- create-handler
  [urls-handler]
  (routes
    (GET "/" [] "running")
    (GET "/urls/:id" [] (partial uh/get-url urls-handler))
    (POST "/urls" [] (partial uh/create-url urls-handler))
    (route/not-found "The requested resource was not found.")))

(defn- wrap-middleware
  [handler]
  (-> handler
      wrap-logging
      wrap-error-handling
      wrap-json-response
      (wrap-json-body {:keywords? true})))

(defmethod ig/init-key :app
  [_ {:keys [urls-handler]}]
  (-> (create-handler urls-handler)
      wrap-middleware))
