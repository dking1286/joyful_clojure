(ns com.shortify.api.app
  (:require [integrant.core :as ig]
            [ring.middleware.json :refer [wrap-json-body
                                          wrap-json-response]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [com.shortify.api.middleware.logging :refer [wrap-logging]]
            [com.shortify.api.middleware.error-handling :refer [wrap-error-handling]]
            [com.shortify.api.urls.handler :as uh]))

(defn- wrap-if
  "Wraps a handler with middleware if 'condition' is true, otherwise leaves
  the handler unmodified."
  [handler condition middleware]
  (if condition
    (middleware handler)
    handler))

(defn- create-handler
  "Composes individual route handlers into the root handler for the
  application."
  [urls-handler]
  (routes
    (GET "/" [] "running")
    (GET "/urls/:id" [] (partial uh/get-url urls-handler))
    (POST "/urls" [] (partial uh/create-url urls-handler))
    (route/not-found "The requested resource was not found.")))

(defn- wrap-middleware
  "Wraps the handler with all middleware needed for the application."
  [handler {:keys [logging?] :or {logging? true}}]
  (-> handler
      (wrap-if logging? wrap-logging)
      wrap-error-handling
      wrap-json-response
      (wrap-json-body {:keywords? true})))

(defmethod ig/init-key :app
  [_ {:keys [urls-handler] :as config}]
  (-> (create-handler urls-handler)
      (wrap-middleware config)))
