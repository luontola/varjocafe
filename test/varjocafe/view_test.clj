(ns varjocafe.view-test
  (:use midje.sweet)
  (:require [varjocafe.view :as view]
            [varjocafe.testdata :as testdata]
            [net.cgrand.enlive-html :as html]
            [clj-time.core :as t]))

(defn render [page] (apply str page))

(fact "Main page"
      ; XXX: Date and food constants must be updated when test data is updated.
      (let [page (render (view/main-page testdata/data (view/today-and-tomorrow testdata/today) testdata/today testdata/settings))]
        (fact "has area names"
              page => (contains "Kumpula"))
        (fact "has area IDs"
              page => (contains "data-area-id=\"2\""))
        (fact "has restaurant names"
              page => (contains "Exactum"))
        (fact "has restaurant IDs"
              page => (contains "data-restaurant-id=\"11\""))
        (fact "has restaurant addresses"
              page => (contains "Gustaf Hällströmin katu 2b, Helsinki"))
        (fact "has restaurant opening times"
              page => (contains "Ma-Pe")
              page => (contains "09:00-15:00"))
        (fact "has restaurant lunch times"
              page => (contains "Ma-Pe")
              page => (contains "11:00-14:00"))
        (fact "has today's menu"
              page => (contains "ke 3.12."))
        (fact "has tomorrow's menu"
              page => (contains "to 4.12."))
        (fact "has foods on menu"
              page => (contains "Broileriwrap"))
        (fact "has food allergens on menu"
              page => (contains "(PÄ, SE, SO, V, soijaa, valkosipulia)"))
        (fact "has food prices on menu"
              page => (contains "Edullisesti"))))

(fact "Google analytics"
      (fact "Enabled"
            (render (view/google-analytics
                      (assoc-in testdata/settings [:google-analytics :tracking-id] "UA-1234567-8")))
            => (contains "ga('create', 'UA-1234567-8'"))
      (fact "Disbled"
            (render (view/google-analytics
                      (assoc-in testdata/settings [:google-analytics :tracking-id] nil)))
            => ""))

(defn select [& args]
  (html/flatten-nodes-coll (apply html/select args)))

(fact "#indented"
      (fact "Selects node and its preceding text node"
            (let [page (html/html [:div "\n" [:h1 "T1"] "\n\n" [:h2 "T2"] "\n\n\n"])]
              (select page [(view/indented :h1)]) => (html/html "\n" [:h1 "T1"])
              (select page [(view/indented :h2)]) => (html/html "\n\n" [:h2 "T2"])))
      (fact "Ignores non-whitespace text nodes"
            (let [page (html/html [:div "\n" [:h1 "T1"] "xxx" [:h2 "T2"] "\n\n\n"])]
              (select page [(view/indented :h2)]) => (html/html [:h2 "T2"])))
      (fact "Ignores missing text nodes"
            (let [page (html/html [:div "\n" [:h1 "T1"] [:h2 "T2"] "\n\n\nx"])]
              (select page [(view/indented :h2)]) => (html/html [:h2 "T2"]))))

(fact "#indent-of"
      (fact "Selects the text node preceding the node"
            (let [page (html/html [:div "\n" [:h1 "T1"] "\n\n" [:h2 "T2"] "\n\n\n"])]
              (select page [(view/indent-of :h1)]) => (html/html "\n")
              (select page [(view/indent-of :h2)]) => (html/html "\n\n")))
      (fact "Ignores non-whitespace text nodes"
            (let [page (html/html [:div "\n" [:h1 "T1"] "xxx" [:h2 "T2"] "\n\n\n"])]
              (select page [(view/indent-of :h2)]) => (html/html)))
      (fact "Ignores missing text nodes"
            (let [page (html/html [:div "\n" [:h1 "T1"] [:h2 "T2"] "\n\n\nx"])]
              (select page [(view/indent-of :h2)]) => (html/html))))

(fact "#today-and-tomorrow"
      (view/today-and-tomorrow (t/local-date 2015 2 10)) => [(t/local-date 2015 2 10) (t/local-date 2015 2 11)])

(fact "#this-week"
      (let [expected [(t/local-date 2015 2 9)
                      (t/local-date 2015 2 10)
                      (t/local-date 2015 2 11)
                      (t/local-date 2015 2 12)
                      (t/local-date 2015 2 13)
                      (t/local-date 2015 2 14)
                      (t/local-date 2015 2 15)]]
        (fact "today is monday"
              (view/this-week (t/local-date 2015 2 9)) => expected)
        (fact "today is wednesday"
              (view/this-week (t/local-date 2015 2 11)) => expected)
        (fact "today is sunday"
              (view/this-week (t/local-date 2015 2 15)) => expected)))

(fact "#next-week"
      (let [expected [(t/local-date 2015 2 16)
                      (t/local-date 2015 2 17)
                      (t/local-date 2015 2 18)
                      (t/local-date 2015 2 19)
                      (t/local-date 2015 2 20)
                      (t/local-date 2015 2 21)
                      (t/local-date 2015 2 22)]]
        (fact "today is monday"
              (view/next-week (t/local-date 2015 2 9)) => expected)
        (fact "today is wednesday"
              (view/next-week (t/local-date 2015 2 11)) => expected)
        (fact "today is sunday"
              (view/next-week (t/local-date 2015 2 15)) => expected)))
