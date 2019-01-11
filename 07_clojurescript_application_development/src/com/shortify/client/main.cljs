(ns ^:figwheel-hooks com.shortify.client.main
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [cljs.user]
            [com.shortify.client.components.app.core :refer [app]]))

(enable-console-print!)

(defn init-view
  []
  (reagent/render [app]
                  (.getElementById js/document "app")))

(.addEventListener js/window "DOMContentLoaded"
                   (fn [e]
                     (init-view)))

(defn ^:after-load on-reload
  []
  (init-view))

(println "hello")
