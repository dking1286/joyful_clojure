(ns cljs.user
  ;; Eagerly require namespaces that would be lazy-loaded in
  ;; the production build
  (:require [reagent.core]))

(enable-console-print!)

(defn hello
  []
  (println "hello world"))
