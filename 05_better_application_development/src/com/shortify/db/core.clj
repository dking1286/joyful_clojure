(ns com.shortify.db.core
  (:import [com.mchange.v2.c3p0 ComboPooledDataSource])
  (:require [clojure.spec.alpha :as s]
            [integrant.core :as ig]
            [environ.core :refer [env]]
            [com.shortify.utils.spec :as su]))

(s/def ::host string?)
(s/def ::port int?)
(s/def ::name string?)
(s/def ::user string?)
(s/def ::password string?)
(s/def ::classname string?)
(s/def ::subprotocol string?)
(s/def ::datasource #(instance? ComboPooledDataSource %))

(s/def ::db-spec
       (s/keys :req-un [::host ::port ::name ::user ::password]))

(s/def ::db-spec-partial
       (s/keys :opt-un [::host ::port ::name ::user ::password]))

(s/def ::db-spec-long-form
       (s/keys :req-un [::classname ::subprotocol ::subname ::user ::password]))

(s/def ::db
       (s/keys :req-un [::datasource]))

(defn- env-spec
  []
  {:host (:database-host env)
   :port (Integer/parseUnsignedInt (:database-port env))
   :name (:database-name env)
   :user (:database-username env)
   :password (:database-password env)})

(defn- db-spec
  [overrides]
  {:pre [(su/valid? ::db-spec-partial overrides)]
   :post [(su/valid? ::db-spec %)]}
  (merge (env-spec) overrides))

(defn- subname
  [host port name]
  (str "//" host "/" port "/" name))

(defn- db-spec-long-form
  [{:keys [host port name user password]}]
  {:post [(su/valid? ::db-spec-long-form %)]}
  {:classname "org.postgresql.jdbc.Driver"
   :subprotocol "postgres"
   :subname (subname host port name)
   :user user
   :password password})

(defn- pool
  [spec]
  {:pre [(su/valid? ::db-spec-long-form spec)]
   :post [(su/valid? ::db %)]}
  (let [cpds (doto (ComboPooledDataSource.)
               (.setDriverClass (:classname spec))
               (.setJdbcUrl (str "jdbc:" (:subprotocol spec) ":" (:subname spec)))
               (.setUser (:user spec))
               (.setPassword (:password spec))
               ;; expire excess connections after 30 minutes of inactivity:
               (.setMaxIdleTimeExcessConnections (* 30 60))
               ;; expire connections after 3 hours of inactivity:
               (.setMaxIdleTime (* 3 60 60)))]
    {:datasource cpds}))

(defmethod ig/init-key :db
  [_ config]
  (-> (db-spec config)
      db-spec-long-form
      pool))
