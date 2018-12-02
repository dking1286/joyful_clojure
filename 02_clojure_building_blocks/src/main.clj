(ns main
  (:require [clojure.string :as string]))

(defn add-7
  [num]
  (let [answer (+ num 7)]
    answer))

(defn hypotenuse
  "Calculates the hypotenuse of a right triangle"
  [side1 side2]
  (let [a-squared (* side1 side1)
        b-squared (* side2 side2)
        c-squared (+ a-squared b-squared)]
    (Math/sqrt c-squared)))

(def users
  [{:id 1 :age 31
    :first-name "Daniel" :last-name "King"}
   {:id 2 :age 16
    :first-name "Angel" :last-name "Herrera"}
   {:id 3 :age 31
    :first-name "Jane" :last-name "Smith"}
   {:id 4 :age 20
    :first-name "Ruth" :last-name "Langley"}])

;; WARNING: This is wrong
(defn get-eligible-user-ids
  [users]
  (let [results []]
    (doseq [user users]
      (if (>= (get user :age) 21)
        (conj results (get user :id))))
    results))

(defn get-eligible-user-ids-1
  [users]
  (loop [remaining users
         results []]
    (if (empty? remaining)
      results
      (let [user (first remaining)
            next-remaining (rest remaining)
            next-results (if (>= (get user :age) 21)
                           (conj results (get user :id))
                           results)]
        (recur next-remaining next-results)))))

(defn get-eligible-user-ids-2
  [users]
  (map (fn [user] (get user :id))
       (filter (fn [user] (>= (get user :age) 21)) users)))

(defn get-eligible-user-ids-3
  [users]
  (reduce conj []
          (map (fn [user] (get user :id))
               (filter (fn [user] (>= (get user :age) 21)) users))))

(defn get-eligible-user-ids-4
  [users]
  (into []
        (map (fn [user] (get user :id))
             (filter (fn [user] (>= (get user :age) 21)) users))))

(defn get-eligible-user-ids-5
  [users]
  (->> users
       (filter (fn [user] (>= (get user :age) 21)))
       (map (fn [user] (get user :id)))
       (into [])))

(def users-table
  {:users-list ["123" "456"]
   :users-by-id {"123" {:id "123"
                        :first-name "Daniel"}
                 "456" {:id "456"
                        :first-name "Jane"}}})
(defn add-new-user
  [users-table user]
  (let [id (get user :id)]
    (-> users-table
        (assoc-in [:users-by-id id] user)
        (update :users-list conj id))))

;; Exercises

(defn get-full-name
  "Gets the full name (first name and last name)
  of a user.

  Example:
  (get-full-name {:first-name \"Daniel\" :last-name \"King\"})
  -> \"Daniel King\""
  [user]
  ;; TODO
  )

(defn get-ids
  "Given a vector of users, returns a list of the users' ids.

  Bonus points: Try doing this with loop/recur and with map."
  [users]
  ;; TODO
  )

(defn http-handler
  "Given an http request, returns the response that should be
  sent back to the client.

  Example request:
  {:method :GET :url \"/hello\"}

  Example response:
  {:status 200 :body \"Hello world\"}

  The handler should support the following routes:
  - GET /hello (responds with \"Hello world\")
  - GET /goodbye (responds with \"Goodbye world\")

  Any other request should result in a 404 response
  with the text \"Not found\" in the body."
  [req]
  ;; TODO
  )

(defn total-of-positives
  "Gets the sum of a sequence of numbers. Non-positive numbers
  should not be included in the sum.

  Example:
  (total-of-positives [1 5 -10 3 -2])
  -> 9"
  [nums]
  ;; TODO
  )

(defn is-palindrome
  "Determines whether or not a given string is a palindrome.

  Examples:
  (is-palindrome \"Hello\") -> false
  (is-palindrome \"abcba\") -> true
  Challenge: (is-palindrome \"Taco Cat\") -> true"
  [str]
  ;; TODO
  )

(defn caesar-encrypt
  "Takes a word and a number, and rotates each letter in the word
  that many characters forward in the alphabet, wrapping around from
  Z back to A.

  Examples:
  (caesar-encrypt \"abc\" 3) -> \"def\"
  (caesar-encrypt \"Zebra\" 4) -> \"Difve\"

  Note: The name of the function comes from the fact that this transformation
  is known as a Caesar Cipher."
  [word num-places]
  ;; TODO
  )
