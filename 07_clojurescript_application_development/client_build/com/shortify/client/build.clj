(ns com.shortify.client.build
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]]
            [sass4clj.core :refer [sass-compile-to-file]]
            [hawk.core :as hawk]
            [figwheel.main.api :as figwheel]))

;; Sass build functions

(def sass-entry "resources/sass/style.scss")
(def sass-output "target/public/sass-out/style.css")
(def sass-source-map-output "target/public/sass-out/style.css.map")
(def sass-default-options {:output-style :compressed
                           :source-map false})
;; TODO: Figure out if the sass-watcher survives reloading
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

(defn build-sass-dev
  []
  (let [sass-options {:output-style :expanded
                      :source-map true}]
    (build-sass sass-options)
    (reset! sass-watcher
            (hawk/watch! [{:paths ["src" "resources"]
                           :filter (fn [ctx e]
                                     (scss-file? (:file e)))
                           :handler (fn [ctx e]
                                      (build-sass sass-options))}]))
    nil))

(defn stop-sass-dev
  []
  (when @sass-watcher
    (hawk/stop! @sass-watcher))
  (reset! sass-watcher nil))

;; Foreign libs build functions

(def webpack-default-options {:mode :production})
(def webpack-watcher (atom nil))

(defn build-foreign-libs
  ([] (build-foreign-libs {}))
  ([options]
   (let [merged-options (merge webpack-default-options options)
         mode (name (:mode merged-options))
         {:keys [exit err out]} (sh "npx" "webpack" (str "--mode=" mode))]
     (when (not (zero? exit))
       (throw (ex-info "Webpack build failed:" {:err err}))))))

(defn build-foreign-libs-dev
  []
  (let [webpack-options {:mode :development}]
    (build-foreign-libs webpack-options)
    (reset! webpack-watcher
            (hawk/watch! [{:paths ["foreign_libs.js"]
                           :handler (fn [ctx e]
                                      (build-foreign-libs webpack-options))}]))
    nil))

(defn stop-foreign-libs-dev
  []
  (when @webpack-watcher
    (hawk/stop! @webpack-watcher))
  (reset! webpack-watcher nil))

;; ClojureScript build functions

(defn build-cljs-dev
  []
  (figwheel/start "dev"))

(defn stop-cljs-dev
  []
  (figwheel/stop "dev"))

(defn build-cljs-prod
  []
  (figwheel/start "prod"))

;; Development build function

(defn start-dev-builds
  []
  (build-sass-dev)
  (build-foreign-libs-dev)
  (build-cljs-dev))

(defn stop-dev-builds
  []
  (stop-sass-dev)
  (stop-foreign-libs-dev)
  (stop-cljs-dev))

(defn cljs-repl
  []
  (figwheel/cljs-repl "dev"))

;; Production build functions

(defn -main
  [& args]
  (build-sass)
  (build-foreign-libs)
  (build-cljs-prod))
