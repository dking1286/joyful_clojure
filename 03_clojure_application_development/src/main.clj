(ns main
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [environ.core :refer [env]]
            [app :refer [app]])
  (:gen-class))

(defn -main
  [& args]
  (run-jetty app {:port (Integer/valueOf (:port env))}))
