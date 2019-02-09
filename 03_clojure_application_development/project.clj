(defproject joyful-clojure-03 "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/data.json "0.2.6"]
                 [ring "1.7.1"]
                 [ring/ring-json "0.4.0"]
                 [ring-logger "1.0.1"]
                 [compojure "1.6.1"]
                 [org.postgresql/postgresql "42.2.5"]
                 [ragtime "0.7.2"]
                 [environ "1.1.0"]
                 [clj-time "0.15.0"]
                 [com.taoensso/timbre "4.10.0"]]

  :plugins [[lein-ring "0.12.4"]
            [lein-environ "1.1.0"]]

  :source-paths ["src"]
  :test-paths ["tests"]
  :target-path "target/%s"

  :ring {:handler app/app
         :port 8080
         :nrepl {:start? true :port 8081}
         :auto-reload? true}

  :profiles
  {:dev {:env {:environment "development"}}

   :test {:env {:environment "test"}
          :dependencies [[pjstadig/humane-test-output "0.9.0"]]
          :injections [(require 'pjstadig.humane-test-output)
                       (pjstadig.humane-test-output/activate!)]}

   :prod {:env {:environment "production"}
          :uberjar-name "app-standalone.jar"
          :main main
          :aot :all}})
