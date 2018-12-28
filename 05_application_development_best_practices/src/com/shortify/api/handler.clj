(ns com.shortify.api.handler
  (:require [com.stuartsierra.component :as component]
            [ring.middleware.json :refer [wrap-json-body
                                          wrap-json-response]]
            [ring.logger]
            [taoensso.timbre :as timbre]
            [com.shortify.api.routes :refer [get-routes]]))

(defn- wrap-error-handling
  [handler-fn]
  (fn [req]
    (try
      (handler-fn req)
      (catch Exception e
        (timbre/log :error e "Internal server error")
        {:status 500 :body "Internal server error"}))))

(defn- log-fn
  [{:keys [level throwable message]}]
  ;; throwable will only be non-nil if there is an uncaught error,
  ;; which should never happen because of the wrap-error-handling middleware
  (if throwable
    (timbre/log level throwable message)
    (timbre/log level message)))

(defn- wrap-logging
  [handler]
  (ring.logger/wrap-with-logger handler {:log-fn log-fn}))

(defprotocol IHandler
  (get-handler [this]))

(defrecord Handler [routes]
  IHandler
  (get-handler [this]
    (-> (get-routes routes)
        (wrap-json-body :keywords? true)
        wrap-json-response
        wrap-error-handling
        wrap-logging)))

(defn handler
  []
  (map->Handler {}))
