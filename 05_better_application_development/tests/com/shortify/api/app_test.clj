(ns com.shortify.api.app-test
  (:require [clojure.test :refer :all]
            [clojure.data.json :as json]
            [clojure.java.jdbc :as jdbc]
            [com.shortify.api.test-helpers :refer [request *system* with-test-system]]))

(use-fixtures :each with-test-system)

(deftest test-urls-routes
  (testing "POST /urls should respond with status 201 on success"
    (let [{:keys [app]} *system*
          res (request app {:uri "/urls"
                            :request-method :post
                            :body {:url "http://website.com"}})]
      (is (= (:status res) 201))))

  (testing "POST /urls should respond with the created record"
    (let [{:keys [app]} *system*
          res (request app {:uri "/urls"
                            :request-method :post
                            :body {:url "http://website.com"}})]
      (is (= (-> res :body :url) "http://website.com"))))

  (testing "POST /urls should create the new record in the database."
    (let [{:keys [app db]} *system*
          res (request app {:uri "/urls"
                            :request-method :post
                            :body {:url "http://website.com"}})
          id (-> res :body :id)
          record (first (jdbc/query db ["SELECT * FROM urls WHERE id = ?" id]))]
      (is (= (:url record) "http://website.com"))))

  (testing "POST /urls should respond with status 400 if the request data is invalid"
    ;; TODO: Write a failing test, then make the test pass
  )

  (testing "POST /urls should respond with status 409 if the id already exists."
    ;; TODO: Write a faliing test, then make the test pass
  )

  (testing "GET /urls/:id should respond with status 200 on success"
    (let [{:keys [app]} *system*
          res (request app {:uri "/urls/12345"
                            :request-method :get})]
      (is (= (:status res) 200))))

  (testing "GET /urls/:id should respond with the database record"
    (let [{:keys [app]} *system*
          res (request app {:uri "/urls/12345"
                            :request-method :get})]
      (is (= (:body res) {:id "12345" :url "https://someawesomewebsite.com"}))))

  (testing "GET /urls/:id should respond with status 404 if no url with the given id exists"
    (let [{:keys [app]} *system*
          res (request app {:uri "/urls/9874398735"
                            :request-method :get})]
      (is (= (:status res) 404)))))
