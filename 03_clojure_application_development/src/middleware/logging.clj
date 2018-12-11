(ns middleware.logging
  (:require [ring.logger]
            [taoensso.timbre :as timbre]))

(defn ^:private log-fn
  [{:keys [level throwable message]}]
  (if throwable
    (timbre/log level throwable message)
    (timbre/log level message)))

(defn wrap-logging
  [handler]
  (ring.logger/wrap-with-logger handler {:log-fn log-fn}))
