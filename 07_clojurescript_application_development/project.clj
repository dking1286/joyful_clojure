(defproject joyful-clojure-07 "1.0.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.339"]
                 [reagent "0.8.1" :exclusions [cljsjs/react
                                               cljsjs/react-dom
                                               cljsjs/create-react-class]]
                 [re-frame "0.10.6" :exclusions [reagent]]]

  :source-paths ["src"]
  :target-path "target/%s"
  :resource-paths ["resources" "target"]
  :clean-targets ^{:protect false} ["target"]

  :profiles
  {:client-build {:dependencies [[deraen/sass4clj "0.3.1"]
                                 [com.bhauman/figwheel-main "0.2.0"]
                                 [cider/piggieback "0.3.8"]
                                 [hawk "0.2.11"]]
                  :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}
                  :source-paths ["client_build"]}

   :dev {:source-paths ["dev"]
         :repl-options {:port 9091}}

   :prod {:source-paths ["prod"]}})
