(ns routes
  (:require [compojure.core :refer :all]
            [compojure.route :refer [not-found]]
            [urls.controller :as urls-controller]))

(defroutes root-handler
  (GET "/urls/:id" [id] urls-controller/get-url)
  (POST "/urls" [] urls-controller/create-url)
  (not-found "Not found"))
