(ns com.shortify.system
  (:require [integrant.core :as ig]
            [environ.core :refer [env]]))

(def config
  {:db {}})

(def test-config
  (merge config {:db {:name "url_shortening_db_test"}}))
