(ns routes-test
  (:refer-clojure :exclude [update])
  (:require [clojure.test :refer :all]
            [cheshire.core :as cheshire]
            [korma.core :refer :all]
            [test-helpers :refer [with-database-reset]]
            [app :refer [app]]
            [urls :refer [urls]]))

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
          body (cheshire/parse-string (:body res) true)]
      (is (= {:id "12345" :url "https://someawesomewebsite.com"}
             body)))))

(deftest test-create-url-route
  (testing "should respond with 201 and the created entity when creation is successful"
    (let [req {:request-method :post
               :uri "/urls"
               :body {:url "https://yetanotherwebsite.com"}}
          res (app req)
          body (cheshire/parse-string (:body res) true)]
      (is (= 201 (:status res)))
      (is (= "https://yetanotherwebsite.com" (:url body)))
      (is (string? (:id body)))))
  (testing "should create the url in the database"
    (let [req {:request-method :post
               :uri "/urls"
               :body {:url "https://website4you.com"}}
          res (app req)
          body (cheshire/parse-string (:body res) true)
          created-entity (first (select urls (where body)))]
      (is (= "https://website4you.com" (:url created-entity)))))
  (testing "should respond with 400 when the input data is invalid"
    ;; TODO: add a failing test, then add code so that it passes
    ))
