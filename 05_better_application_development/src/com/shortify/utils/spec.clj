(ns com.shortify.utils.spec
  (:require [clojure.spec.alpha :as s]))

(defn valid?
  [spec val]
  (if (s/valid? spec val)
    true
    (do
      (s/explain spec val)
      false)))
