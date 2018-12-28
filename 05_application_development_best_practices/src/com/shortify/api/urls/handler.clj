(ns com.shortify.api.urls.handler
  (:require [ring.util.response :refer :all]
            [com.shortify.api.urls.service :as urls-service]))

(defprotocol IUrlsHandler
  (get-url [this req])
  (create-url [this req]))

(defrecord UrlsHandler [urls-service]
  IUrlsHandler
  (get-url [this req]
    (let [id (-> req :params :id)
          url (urls-service/get-url urls-service id)]
      (-> (response {:data url})
          (status 200))))

  (create-url [this req]
    (let [data (-> req :body)
          new-url (urls-service/create-url urls-service data)]
      (-> (response {:data new-url})
          (status 201)))))

(defn urls-handler
  []
  (map->UrlsHandler {}))
