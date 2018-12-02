(ns solutions
  (:require [clojure.string :as string]))

(defn get-full-name
  [user]
  (let [first-name (get user :first-name)
        last-name (get user :last-name)]
    (str first-name " " last-name)))

(defn get-ids
  "Solution 1: With loop/recur"
  [users]
  (loop [remaining users
         ids []]
    (if (empty? remaining)
      ids
      (recur (rest remaining) (conj ids (get (first remaining) :id))))))

(defn get-ids-2
  "Solution 2: With map"
  [users]
  (->> users
       (map (fn [user] (get user :id)))
       (into [])))

(defn get-ids-3
  "Solution 3: A keyword can be used as a function to retrieve the
  corresponding value in a map:

  (:first-name {:first-name \"Daniel\" :last-name \"King\"})
  -> \"Daniel\"

  This let's us write Solution 2 much more concisely."
  [users]
  (->> users
       (map :id)
       (into [])))

(defn http-handler
  [req]
  (let [method (get req :method)
        url (get req :url)]
    (cond
      (and (= method :GET) (= url "/hello")) {:status 200
                                              :body "Hello world"}
      (and (= method :GET) (= url "/goodbye")) {:status 200
                                                :body "Goodbye world"}
      :else {:status 404
             :body "Not found"})))

(defn http-handler-2
  "Solution 2: Using destructuring to extract method and url more easily."
  [req]
  (let [{:keys [method url]} req]
    (cond
      (and (= method :GET) (= url "/hello")) {:status 200
                                              :body "Hello world"}
      (and (= method :GET) (= url "/goodbye")) {:status 200
                                                :body "Goodbye world"}
      :else {:status 404
             :body "Not found"})))

(defmulti http-handler-3
  "Solution 3: Using a 'multimethod', a way of defining multiple implementations
  of a function depending on what gets passed in as an argument."
  (fn [req] [(get req :method) (get req :url)]))

(defmethod http-handler-3 [:GET "/hello"]
  [req]
  {:status 200 :body "Hello world"})

(defmethod http-handler-3 [:GET "/goodbye"]
  [req]
  {:status 200 :body "Goodbye world"})

(defmethod http-handler-3 :default
  [req]
  {:status 404 :body "Not found"})

(defn total-of-positives
  "Solution 1: With loop/recur"
  [nums]
  (loop [remaining nums
         total 0]
    (if (empty? remaining)
      total
      (let [next-num (first remaining)
            next-remaining (rest remaining)
            next-total (if (> next-num 0)
                         (+ total next-num)
                         total)]
        (recur next-remaining next-total)))))

(defn total-of-positives-2
  "Solution 2: With sequence functions and the thread-last macro"
  [nums]
  (->> nums
       (filter (fn [num] (> num 0)))
       (reduce +)))

(defn is-palindrome
  "Solution 1: Not ignoring capitals and spaces.

  Uses 'first' and 'last' to ensure that the first and last characters are
  equal, and recursively calls the function on the rest of the characters."
  [str]
  (if (empty? str)
    true
    (and (= (first str) (last str))
         (is-palindrome (butlast (rest str))))))

(defn is-palindrome-2
  "Solution 2: Non-recursive

  (reverse str) returns a sequence of one-element strings, in the
  opposite order. In order to use the = operator to compare this with the
  original string, we need to call (sequence str) to convert the original
  string into a sequence as well."
  [str]
  (= (sequence str) (reverse str)))

(defn is-palindrome-3
  "Solution 3: Challenge, ignoring capitals and spaces.

  (is-palindrome-3 \"Taco Cat\") -> true

  This solution uses the clojure.string library, imported at the top of
  this file."
  [str]
  (let [cleaned-string (-> str
                           (string/upper-case)
                           (string/replace " " ""))]
    (= (sequence cleaned-string) (reverse cleaned-string))))

(def alphabet "abcdefghijklmnopqrstuvwxyz")

(defn rotate-character
  "Helper function for caesar-encrypt. Takes a length-one string and returns
  a length-one string representing the character num-places spaces forward in
  the alphabet, wrapping around at z. Preserves capitalization."
  [character num-places]
  (let [lower-character (string/lower-case character)
        index (string/index-of alphabet lower-character)
        new-index (mod (+ index num-places) (count alphabet))
        new-character (nth alphabet new-index)]
    (if (= character lower-character)
      new-character
      (string/upper-case new-character))))

(defn caesar-encrypt
  [word num-places]
  (->> word
       (map (fn [char] (rotate-character (str char) num-places)))
       (string/join "")))
