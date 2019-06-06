(ns com.shortify.api.app
  (:require [integrant.core :as ig]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [com.shortify.api.urls.handler :as uh]))

(defn create-handler
  [urls-handler]
  (routes
    (GET "/" [] "running")
    (GET "/urls/:id" [] (partial uh/get-url urls-handler))
    (POST "/urls" [] (partial uh/create-url urls-handler))
    (route/not-found "The requested resource was not found.")))

(defn wrap-middleware
  [handler]
  handler)

(defmethod ig/init-key :app
  [_ {:keys [urls-handler]}]
  (-> (create-handler urls-handler)
      wrap-middleware))
