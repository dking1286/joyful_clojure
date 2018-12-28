(ns com.shortify.api.db.seed
  (:require [clojure.spec.alpha :as s]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [com.stuartsierra.component :as component]))

(s/def ::insert-fn qualified-symbol?)
(s/def ::component keyword?)
(s/def ::data (s/coll-of map? :kind vector?))

(s/def ::seed (s/keys :req-un [::insert-fn
                               ::component
                               ::data]))

(defn- insert-seed-values
  [insert-fn component data]
  (doseq [value data]
    (insert-fn component value)))

(defn- insert-seed
  [db-seeder seed]
  {:pre [(s/valid? ::seed seed)]}
  (let [{insert-fn-name :insert-fn
         component-name :component
         data :data} seed
        insert-fn-var (resolve insert-fn-name)
        component (get db-seeder component-name)]
    (cond
      (nil? insert-fn-var)
      (throw (ex-info (str "insert-fn " insert-fn-name " not found")
                      {:type :seed-error}))

      (nil? component)
      (throw (ex-info (str "component " component-name " not included as dependency in db-seeder")
                      {:type :seed-error}))

      :else
      (insert-seed-values @insert-fn-var component data))))

(defn- get-seed-files
  [db-seeder]
  (let [{:keys [resource-path]} db-seeder]
    (.listFiles (io/file (io/resource resource-path)))))

(defprotocol IDbSeeder
  (insert-all-seeds! [this]))

(defrecord DbSeeder [resource-path]
  component/Lifecycle
  (start [this] this)
  (stop [this] this)

  IDbSeeder
  (insert-all-seeds! [this]
    (let [seed-files (get-seed-files)]
      (doseq [file seed-files]
        (let [seed (edn/read-string (slurp file))]
          (insert-seed this seed))))))

(defn db-seeder
  [{:keys [resource-path] :as opts}]
  (map->DbSeeder opts))
