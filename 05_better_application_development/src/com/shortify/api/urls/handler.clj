(ns com.shortify.api.urls.handler
  (:require [clojure.java.jdbc :as jdbc]
            [integrant.core :as ig]
            [com.shortify.api.urls.core :as urls-core]
            [com.shortify.api.utils.errors :as errors]))

(defn get-url
  [db req]
  (let [id (get-in req [:params :id])
        url (urls-core/get-url db id)]
    (if-not url
      (throw (errors/not-found))
      {:status 200 :body url})))

(defn create-url
  [db req]
  (let [row (urls-core/create-url db (:body req))]
    {:status 201 :body row}))
