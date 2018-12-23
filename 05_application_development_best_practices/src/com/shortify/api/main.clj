(ns com.shortify.api.main
  (:require [com.stuartsierra.component :as component]
            [environ.core :refer [env]]
            [com.shortify.api.system :refer [system]]))

(defn -main
  [& argv]
  (component/start (system env)))
