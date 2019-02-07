(ns db.core
  (:require [korma.db :refer [defdb postgres]]
            [environ.core :refer [env]]))

(def spec
  (postgres {:db (:database-name env)
             :user (:database-username env)
             :password (:database-password env)
             :host (:database-host env)
             :port (:database-port env)}))

(defdb db spec)
