(ns com.shortify.client.build
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]]
            [com.stuartsierra.component :as component]
            [ring.util.response :refer [file-response]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [hiccup.page :refer [html5]]
            [sass4clj.core :refer [sass-compile-to-file]]
            [hawk.core :as hawk]
            [figwheel.main.api :as figwheel]))

;; HTML build functions

(def html-output "target/public/index.html")
(def html-default-options
  {:styles ["sass-out/style.css"]
   :scripts ["cljs-out/cljs_base.js"
             "cljs-out/vendor.js"
             "cljs-out/main.js"]
   :defer-scripts? true})

(defn create-html
  [{:keys [styles scripts defer-scripts?]}]
  (html5
   [:head
    [:title "Shortify"]
    (for [style styles]
      [:link {:type "text/css", :href style, :rel "stylesheet"}])
    (for [script scripts]
      [:script {:type "text/javascript" :src script :defer defer-scripts?}])]
   [:body
    [:div.app]]))

(defn build-html
  ([] (build-html {}))
  ([options]
   (let [merged-options (merge html-default-options options)
         html (create-html merged-options)]
     (io/make-parents html-output)
     (spit html-output html))))

(defrecord HtmlDevelopmentBuilder []
  component/Lifecycle
  (start [this]
    (build-html {:scripts ["cljs-out/main.js"]
                 :defer-scripts? false})
    this)
  (stop [this]
    this))

(defn html-development-builder
  []
  (map->HtmlDevelopmentBuilder {}))

;; Sass build functions

(def sass-entry "resources/sass/style.scss")
(def sass-output "target/public/sass-out/style.css")
(def sass-source-map-output "target/public/sass-out/style.css.map")
(def sass-default-options {:output-style :compressed
                           :source-map false})
(defonce sass-watcher (atom nil))

(defn- scss-file?
  [file]
  (string/ends-with? (.getName file) ".scss"))

(defn build-sass
  ([] (build-sass {}))
  ([options]
   (sass-compile-to-file
    sass-entry sass-output (merge sass-default-options options))
   nil))

(defrecord SassDevelopmentBuilder []
  component/Lifecycle
  (start [this]
    (println "Starting sass builder...")
    (let [sass-options {:output-style :expanded
                        :source-map true}
          watcher (hawk/watch! [{:paths ["src" "resources"]
                                 :filter (fn [ctx e]
                                           (scss-file? (:file e)))
                                 :handler (fn [ctx e]
                                            (build-sass sass-options))}])]
      (build-sass sass-options)
      (println "Sass builder started!")
      (assoc this :watcher watcher)))

  (stop [this]
    (let [watcher (:watcher this)]
      (when watcher
        (hawk/stop! watcher))
      (assoc this :watcher nil))))

(defn sass-development-builder
  []
  (map->SassDevelopmentBuilder {}))

;; Foreign libs build functions

(def webpack-default-options {:mode :production})

(defn build-foreign-libs
  ([] (build-foreign-libs {}))
  ([options]
   (let [merged-options (merge webpack-default-options options)
         mode (name (:mode merged-options))
         {:keys [exit err out]} (sh "npx" "webpack" (str "--mode=" mode))]
     (when (not (zero? exit))
       (throw (ex-info "Webpack build failed:" {:err err}))))))

(defrecord ForeignLibsDevelopmentBuilder []
  component/Lifecycle
  (start [this]
    (println "Starting foreign_libs build...")
    (let [webpack-options {:mode :development}
          watcher (hawk/watch! [{:paths ["foreign_libs.js"]
                                 :handler (fn [ctx e]
                                            (build-foreign-libs webpack-options))}])]
      (build-foreign-libs webpack-options)
      (println "Foreign libs build started!")
      (assoc this :watcher watcher)))

  (stop [this]
    (let [watcher (:watcher this)]
      (when watcher
        (hawk/stop! watcher))
      (assoc this :watcher nil))))

(defn foreign-libs-development-builder
  []
  (map->ForeignLibsDevelopmentBuilder {}))

;; Development server handler

(def handler
  (routes
   (GET "/" [] "hello")
   (route/files "/" {:root "public"})
   (route/not-found "Not found")))

;; ClojureScript build functions

(def cljs-compiler-default-options
  {:main 'com.shortify.client.main})

(def cljs-compiler-dev-options
  {:output-to "target/public/cljs-out/main.js"})

(def cljs-compiler-prod-options
  {}) ;; FIXME

(def figwheel-dev-options
  {:id "dev"
   ;; ClojureScript compiler options
   :options (merge cljs-compiler-default-options
                   cljs-compiler-dev-options)
   ;; Figwheel configuration options
   :config {:watch-dirs ["src" "dev"]
            :css-dirs ["target/public/sass-out"]
            :ring-server-options {:port 9092}
            :ring-handler 'com.shortify.client.build/handler
            :auto-testing true
            :ansi-colors false
            :mode :serve
            :npm {:bundles {"target/public/foreign_libs.bundle.js" "foreign_libs.js"}}}})

(def figwheel-prod-options
  {}) ;; FIXME

(defrecord CljsDevelopmentBuilder []
  component/Lifecycle
  (start [this]
    (try
      (println "Starting cljs build...")
      (figwheel/start figwheel-dev-options)
      (println "Cljs build started!")
      (assoc this :started? true)
      (catch RuntimeException e
        (println "Error while starting cljs builder: ")
        (println e)
        this)))

  (stop [this]
    (when (:started? this)
      (figwheel/stop (:id figwheel-dev-options)))
    (assoc this :started? nil)))

(defn cljs-development-builder
  []
  (map->CljsDevelopmentBuilder {}))

(defn cljs-repl
  []
  (figwheel/cljs-repl (:id figwheel-dev-options)))

;; Development build functions

(defn dev-build-system
  []
  (component/system-map
   :html-development-builder (html-development-builder)
   :sass-development-builder (sass-development-builder)
   :foreign-libs-development-builder (foreign-libs-development-builder)
   :cljs-development-builder (cljs-development-builder)))
