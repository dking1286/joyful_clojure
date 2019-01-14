(ns com.shortify.client.build
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]]
            [hiccup.page :refer [html5]]
            [sass4clj.core :refer [sass-compile-to-file]]
            [hawk.core :as hawk]
            [figwheel.main.api :as figwheel]))

;; HTML build functions

(def html-output "target/public/index.html")

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

(defn build-html-dev
  []
  (let [html (create-html {:styles ["sass-out/style.css"]
                           :scripts ["cljs-out/main.js"]
                           :defer-scripts? false})]
    (io/make-parents html-output)
    (spit html-output html)))

(defn build-html-prod
  []
  (let [html (create-html {:styles ["sass-out/style.css"]
                           :scripts ["cljs-out/cljs_base.js"
                                     "cljs-out/vendor.js"
                                     "cljs-out/main.js"]
                           :defer-scripts? true})]
    (io/make-parents html-output)
    (spit html-output html)))

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
  (build-html-dev)
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

(defn execute-prod-builds
  []
  (build-html-prod)
  (build-sass)
  (build-foreign-libs)
  (build-cljs-prod))

(defn -main
  [& args]
  (execute-prod-builds))
