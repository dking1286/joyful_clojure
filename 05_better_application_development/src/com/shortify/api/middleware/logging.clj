(ns com.shortify.api.middleware.logging
  (:require [ring.logger]
            [taoensso.timbre :as timbre]))

(defn- log-fn
  [{:keys [level throwable message]}]
  (if throwable
    (timbre/log level throwable message)
    (timbre/log level message)))

(defn wrap-logging
  "Ring middleware that logs each request and response."
  [handler]
  (ring.logger/wrap-with-logger handler {:log-fn log-fn}))
