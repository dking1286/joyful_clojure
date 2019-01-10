(defproject joyful-clojure-07 "1.0.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.339"]
                 [reagent "0.8.1" :exclusions [cljsjs/react
                                               cljsjs/react-dom
                                               cljsjs/create-react-class]]
                 [re-frame "0.10.6" :exclusions [reagent]]]

  :source-paths ["src"]
  :target-path "target/%s"

  :cljsbuild
  {:builds {:main {:source-paths ["src"]
                   :compiler {:main com.shortify.client.main
                              :asset-path "cljs-out"
                              :output-to "resources/public/cljs-out/main.js"
                              :output-dir "resources/public/cljs-out"
                              :npm-deps false
                              :foreign-libs [{:file "resources/public/webpack-out/foreign_libs.js"
                                              :provides ["react"
                                                         "react-dom"
                                                         "create-react-class"
                                                         "material-ui"
                                                         "material-ui-icons"]
                                              :global-exports {react React
                                                               react-dom ReactDOM
                                                               create-react-class createReactClass
                                                               material-ui MaterialUi
                                                               material-ui-icons MaterialUiIcons}}]}}}}

  :profiles
  {:fig {:dependencies [[figwheel-sidecar "0.5.4-6"]
                        [cider/piggieback "0.3.8"]]
         :plugins [[lein-figwheel "0.5.17"]]
         :nrepl-middleware [cider.piggieback/wrap-cljs-repl]
         :cljsbuild {:builds {:main {:figwheel {:on-jsload com.shortify.client.main/on-reload}}}}
         :figwheel {:http-server-root "public"
                    :server-port 9090
                    :css-dirs ["resources/public/sass-out"]
                    :nrepl-port 9091}}
   :dev {:source-paths ["dev"]
         :cljsbuild {:builds {:main {:source-paths ["dev"]
                                     :compiler {:optimizations :none}}}}}
   :prod {:cljsbuild {:builds {:main {:source-paths ["prod"]
                                      :compiler {:optimizations :advanced
                                                 :infer-externs true
                                                 :elide-asserts true}}}}}})
