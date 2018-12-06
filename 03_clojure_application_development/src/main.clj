(ns main
  (:require [ring.middleware.json :refer [wrap-json-body
                                          wrap-json-response]]
            [routes :refer [root-handler]]))

(def app
  (-> root-handler
      wrap-json-response
      (wrap-json-body {:keywords? true})))
