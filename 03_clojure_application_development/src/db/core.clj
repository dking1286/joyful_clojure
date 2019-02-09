(ns db.core
  (:require [environ.core :refer [env]]))

(def connection
  {:dbtype (:database-type env)
   :dbname (:database-name env)
   :user (:database-username env)
   :password (:database-password env)
   :host (:database-host env)
   :port (:database-port env)})
