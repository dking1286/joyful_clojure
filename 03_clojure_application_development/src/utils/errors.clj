(ns utils.errors)

(defn not-found
  "Creates an error indicating that a requested resource was not found."
  []
  (ex-info "Not found" {:type :not-found}))
