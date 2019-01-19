(ns com.shortify.client.server.app
  (:require [clojure.java.io :as io]
            [com.stuartsierra.component :as component]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.file :refer [wrap-file]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [ring.util.response :refer [file-response]]
            [compojure.core :refer :all]
            [compojure.route :refer [not-found]]))

(defn- create-root-handler
  [this]
  (routes
   (GET "/" [] (file-response "target/public/index.html"))
   (not-found "The requested resource was not found")))

(defn- create-handler
  [this root-handler]
  (-> root-handler
      (wrap-file "target/public")
      (wrap-content-type)
      (wrap-not-modified)))

(defrecord App [port]
  component/Lifecycle
  (start [this]
    (println "Starting app...")
    (io/make-parents "target/public/index.html")
    (let [server-promise (promise)
          root-handler (create-root-handler this)
          handler (create-handler this root-handler)]
      (run-jetty handler {:port port
                          :join? false
                          :configurator (fn [server]
                                          (deliver server-promise server))})
      (println (str "Server listening on port " port))
      (assoc this :server @server-promise)))

  (stop [this]
    (let [server (:server this)]
      (when server)
      (.stop server)
      (assoc this :server nil))))

(defn app
  [{:keys [port]}]
  (map->App {:port port}))
