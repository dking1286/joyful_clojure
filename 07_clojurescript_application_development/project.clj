(defproject joyful-clojure-07 "1.0.0-SNAPSHOT"
            :dependencies [[org.clojure/clojure "1.9.0"]
                           [org.clojure/clojurescript "1.10.339"]
                           [org.clojure/core.async "0.4.490"]
                           [com.stuartsierra/component "0.4.0"]
                           [environ "1.1.0"]
                           [ring "1.7.0"]
                           [compojure "1.6.1"]
                           [reagent "0.8.1"
                            :exclusions [cljsjs/react
                                         cljsjs/react-dom
                                         cljsjs/create-react-class]]
                           [re-frame "0.10.6"
                            :exclusions [reagent]]]

            :plugins [[lein-environ "1.1.0"]]

            :source-paths ["src"]
            :target-path "target/%s"
            :resource-paths ["resources" "target"]
            :clean-targets ^{:protect false} ["target"]

            :profiles
            {:client-build {:dependencies [[hiccup "1.0.5"]
                                           [deraen/sass4clj "0.3.1"]
                                           [com.bhauman/figwheel-main "0.2.0"
                                            :exclusions [ring]]
                                           [cider/piggieback "0.3.8"]
                                           [hawk "0.2.11"]]
                            :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}
                            :source-paths ["client_build"]}

             :dev {:source-paths ["dev"]
                   :repl-options {:port 9091}}

             :prod {:source-paths ["prod"]}})
