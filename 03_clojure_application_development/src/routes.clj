(ns routes
  (:require [compojure.core :refer :all]
            [compojure.route :refer [not-found]]
            [urls]))

(defroutes root-handler
  (GET "/urls/:id" [] urls/get-url-handler)
  (POST "/urls" [] urls/create-url-handler)
  (not-found "Not found"))
