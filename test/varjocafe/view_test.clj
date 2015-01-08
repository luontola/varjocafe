(ns varjocafe.view-test
  (:use midje.sweet)
  (:require [varjocafe.view :as view]
            [varjocafe.testdata :as testdata]
            [net.cgrand.enlive-html :as html]))

(fact "Main page"
      ; XXX: Date and food constants must be updated when test data is updated.
      (let [page (apply str (view/main-page testdata/data testdata/today testdata/settings))]
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
              page => (contains "(PÄ, SE, SO, V, soijaa, valkosipulia)"))))

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
