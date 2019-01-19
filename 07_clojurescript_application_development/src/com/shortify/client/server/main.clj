(ns com.shortify.client.server.main
  (:require [clojure.java.io :as io]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.file :refer [wrap-file]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [ring.util.response :refer [file-response]]
            [compojure.core :refer :all]
            [compojure.route :refer [not-found]]))

(io/make-parents "target/public/index.html")

(defroutes root-handler
  (GET "/" [] (file-response "target/public/index.html"))
  (not-found "The requested resource was not found"))

(def handler
  (-> root-handler
      (wrap-file "target/public")
      (wrap-content-type)
      (wrap-not-modified)))

(defn start-server
  []
  (run-jetty handler {:port 9090}))
