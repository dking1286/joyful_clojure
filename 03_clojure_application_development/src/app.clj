(ns app
  (:require [ring.middleware.json :refer [wrap-json-body
                                          wrap-json-response]]
            [middleware.logging :refer [wrap-logging]]
            [middleware.error-handling :refer [wrap-error-handling]]
            [routes :refer [root-handler]]))

(def app
  (-> root-handler
      wrap-error-handling
      wrap-logging
      wrap-json-response
      (wrap-json-body {:keywords? true})))
