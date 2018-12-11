(ns middleware.error-handling)

(defn get-response-from-error
  [error]
  (let [type (get (ex-data error) :type)]
    (case type
      :not-found {:status 404 :body "Not found"}
      {:status 500 :body "Internal server error"})))

(defn wrap-error-handling
  [handler]
  (fn [req]
    (try
      (handler req)
      (catch Exception e
        (get-response-from-error e)))))
