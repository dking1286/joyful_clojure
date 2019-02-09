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
      ;; Serialize the response body into JSON
      wrap-json-response
      ;; If the request body contains JSON, parse it into a Clojure
      ;; data structure, converting string keys to keywords
      (wrap-json-body {:keywords? true})))
