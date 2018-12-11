(ns utils.errors)

(defn not-found
  []
  (ex-info "Not found" {:type :not-found}))
