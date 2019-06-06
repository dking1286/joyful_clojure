(ns com.shortify.api.middleware.error-handling)

(defn get-response-from-error
  "Converts a thrown error into the response that should be
   sent back to the client."
  [error]
  (let [type (get (ex-data error) :type)]
    (case type
      :not-found {:status 404 :body "Not found"}
      {:status 500 :body "Internal server error"})))

(defn wrap-error-handling
  "Ring middleware that catches any thrown errors and sends an appropriate
   response back to the client."
  [handler]
  (fn [req]
    (try
      (handler req)
      (catch Exception e
        (get-response-from-error e)))))
