(ns db.core
  (:require [environ.core :refer [env]]))

(def connection
  "Map representing the database connection."
  {:dbtype (:database-type env)
   :dbname (:database-name env)
   :user (:database-username env)
   :password (:database-password env)
   :host (:database-host env)
   :port (:database-port env)})
