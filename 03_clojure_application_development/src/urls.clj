(ns urls
  (:require [clojure.java.jdbc :as jdbc]
            [db.core :refer [connection]]
            [utils.errors :refer [not-found]]))

(defn get-url-by-id
  "Gets the url from the database with the given id, or nil if no such
   url exists."
  [id]
  (let [query ["SELECT * FROM urls WHERE id = ?" id]
        result (jdbc/query connection query)]
    (first result)))

(defn create-url!
  "Given a url as a string, creates a url row in the database and returns
   the created row."
  [url]
  (let [id (.toString (java.util.UUID/randomUUID))
        row {:url url :id id}
        result (jdbc/insert! connection :urls row)]
    (first result)))

(defn get-url-handler
  [req]
  (let [id (get-in req [:params :id])
        url (get-url-by-id id)]
    (if-not url
      (throw (not-found))
      {:status 200 :body url})))

(defn create-url-handler
  [req]
  (let [url (get-in req [:body :url])
        row (create-url! url)]
    {:status 201 :body row}))
