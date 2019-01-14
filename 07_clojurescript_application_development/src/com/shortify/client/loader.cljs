(ns com.shortify.client.loader
  (:require [clojure.core.async :refer [chan go <! >!]]
            [cljs.loader :as loader]
            [com.shortify.client.config :as config]))

(defn set-loaded!
  [module-name]
  (when (not= config/environment "dev")
    (loader/set-loaded! module-name)))

(defn- load-or-ignore-in-development
  [module-name cb]
  (if (= config/environment "dev")
    (cb)
    (loader/load module-name cb)))

(defn loaded?
  [module-name]
  (if (= config/environment "dev")
    true
    (loader/loaded? module-name)))

(defn load
  [module-name]
  (let [out-chan (chan)]
    (load-or-ignore-in-development
     module-name
     (fn []
       (set-loaded! module-name)
       (go (>! out-chan module-name))))
    out-chan))
