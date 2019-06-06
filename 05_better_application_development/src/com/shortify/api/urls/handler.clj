(ns com.shortify.api.urls.handler
  (:require [clojure.java.jdbc :as jdbc]
            [integrant.core :as ig]
            [com.shortify.api.urls.core :as urls-core]
            [com.shortify.api.utils.errors :as errors]))

(defprotocol IUrlsHandler
  (get-url [this req])
  (create-url [this req]))

(defrecord UrlsHandler [urls]
  IUrlsHandler
  (get-url [this req]
    (let [id (get-in req [:params :id])
          url (urls-core/get-url urls id)]
      (if-not url
        (throw (errors/not-found))
        {:status 200 :body url})))

  (create-url [this req]
    (let [row (urls-core/create-url urls (:body req))]
      {:status 201 :body row})))

(defmethod ig/init-key :urls-handler
  [_ {:keys [factory config]}]
  (factory config))
