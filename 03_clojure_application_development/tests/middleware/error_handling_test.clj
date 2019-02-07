(ns middleware.error-handling-test
  (:require [clojure.test :refer :all]
            [middleware.error-handling :refer :all]))

(deftest test-get-response-from-error
  (testing "should return a 404 response when the exception data
            includes :type :not-found"
    (let [e (ex-info "Boom" {:type :not-found})
          response (get-response-from-error e)]
      (is (= (:status response) 404))))
  (testing "should return a 500 response otherwise"
    (let [e (ex-info "Boom" {:type :wrong})
          response (get-response-from-error e)]
      (is (= (:status response) 500)))))
