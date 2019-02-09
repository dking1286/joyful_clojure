(ns routes-test
  (:refer-clojure :exclude [update])
  (:require [clojure.test :refer :all]
            [clojure.java.jdbc :as jdbc]
            [clojure.data.json :as json]
            [test-helpers :refer [with-database-reset]]
            [app :refer [app]]
            [db.core :refer [connection-2]]))

(use-fixtures :each with-database-reset)

(deftest test-get-url-route
  (testing "should respond with a 404 if the specified url does not exist"
    (let [req {:request-method :get
               :uri "/urls/98765"}
          res (app req)]
      (is (= (:status res)
             404))))
  (testing "should respond with the url if it exists."
    (let [req {:request-method :get
               :uri "/urls/12345"}
          res (app req)
          body (json/read-str (:body res)
                              :key-fn keyword)]
      (is (= {:id "12345" :url "https://someawesomewebsite.com"}
             body)))))

(deftest test-create-url-route
  (testing "should respond with 201 and the created entity when creation is successful"
    (let [req {:request-method :post
               :uri "/urls"
               :body {:url "https://yetanotherwebsite.com"}}
          res (app req)
          body (json/read-str (:body res)
                              :key-fn keyword)]
      (is (= 201 (:status res)))
      (is (= "https://yetanotherwebsite.com" (:url body)))
      (is (string? (:id body)))))
  (testing "should create the url in the database"
    (let [req {:request-method :post
               :uri "/urls"
               :body {:url "https://website4you.com"}}
          res (app req)
          query ["SELECT * FROM urls WHERE url = ?" "https://website4you.com"]
          result (jdbc/query connection-2 query)
          created-entity (first result)]
      (is (= "https://website4you.com" (:url created-entity)))))
  (testing "should respond with 400 when the input data is invalid"
    ;; TODO: add a failing test, then add code so that it passes
))
