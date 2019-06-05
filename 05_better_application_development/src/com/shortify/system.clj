(ns com.shortify.system
  (:require [integrant.core :as ig]
            [environ.core :refer [env]]))

(def config
  {:db {}
   :urls {:db (ig/ref :db)}
   :routes {:urls (ig/ref :urls)}
   :app {:routes (ig/ref :routes)}
   :server {:handler (ig/ref :app)}})

(def test-config
  (merge config {:db {:name "url_shortening_db_test"}}))
