(ns main)

(def my-name "Daniel King")

(defn hello-message
  [name]
  (str "Hello " name))

(defn -main
  [& args]
  (println (hello-message my-name)))
