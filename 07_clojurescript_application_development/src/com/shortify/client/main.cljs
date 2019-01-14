(ns ^:figwheel-hooks com.shortify.client.main
  (:require [clojure.core.async :refer [chan go <! >!]]
            [cljs.user]
            [com.shortify.client.loader :as loader]
            [com.shortify.client.components.app.core :refer [app]]))

(defn wait-until-dom-loaded
  []
  (let [channel (chan)]
    (.addEventListener js/window "DOMContentLoaded"
                       (fn [e]
                         (go (>! channel e))))
    channel))

(defn render
  [& args]
  (apply (resolve 'reagent.core/render) args))

(defn init-view
  []
  (render
    [app]
    (.querySelector js/document ".app")))

(defn init-app
  []
  (let [vendor-loaded (loader/load :vendor)
        dom-loaded (wait-until-dom-loaded)]
    (go
      (<! vendor-loaded)
      (<! dom-loaded)
      (init-view))))

(defn ^:after-load on-reload
  []
  (init-view))

(init-app)
