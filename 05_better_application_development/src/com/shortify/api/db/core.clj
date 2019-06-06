(ns com.shortify.api.db.core
  (:import [com.mchange.v2.c3p0 ComboPooledDataSource])
  (:require [clojure.spec.alpha :as s]
            [integrant.core :as ig]
            [environ.core :refer [env]]
            [com.shortify.api.utils.spec :as su]))

(s/def ::host string?)
(s/def ::port int?)
(s/def ::dbtype #{"postgresql"})
(s/def ::dbname string?)
(s/def ::user string?)
(s/def ::password string?)
(s/def ::classname string?)
(s/def ::subprotocol string?)
(s/def ::datasource #(instance? ComboPooledDataSource %))

(s/def ::db-spec
       (s/keys :req-un [::host ::port ::dbname ::user ::password]))

(s/def ::db-spec-partial
       (s/keys :opt-un [::host ::port ::dbname ::user ::password]))

(s/def ::db-spec-long-form
       (s/keys :req-un [::classname ::subprotocol ::subname ::user ::password]))

(s/def ::db
       (s/keys :req-un [::datasource]))

(defn- env-spec
  "Extracts database configuration options from environment variables."
  []
  {:host (:database-host env)
   :port (Integer/parseUnsignedInt (:database-port env))
   :dbtype (:database-type env)
   :dbname (:database-name env)
   :user (:database-username env)
   :password (:database-password env)})

(defn- db-spec
  "Merges database configuration options from environment variables with a map
  of overrides."
  [overrides]
  {:pre [(su/valid? ::db-spec-partial overrides)]
   :post [(su/valid? ::db-spec %)]}
  (merge (env-spec) overrides))

(defn- subname
  "Constructs the subname required by the 'long-form' database spec."
  [host port name]
  (str "//" host ":" port "/" name))

(defn- classname
  "Gets the classname of the driver associated with a database type."
  [dbtype]
  (case dbtype
    "postgresql" "org.postgresql.Driver"
    "mysql" "com.mysql.jdbc.Driver"
    nil))

(defn- db-spec-long-form
  "Constructs the 'long-form' database spec required by the c3p0 connection
  pooling library."
  [{:keys [host port dbname dbtype user password]}]
  {:post [(su/valid? ::db-spec-long-form %)]}
  {:classname (classname dbtype)
   :subprotocol dbtype
   :subname (subname host port dbname)
   :user user
   :password password})

(defn- pool
  "Creates a map containing a connection pool for the configured database.
  This map can be passed in as the first argument of all clojure.java.jdbc
  functions."
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
