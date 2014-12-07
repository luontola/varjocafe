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
        (fact "has restaurant names"
              page => (contains "Exactum"))
        (fact "has today's menu"
              page => (contains "ke 3.12."))
        (fact "has tomorrow's menu"
              page => (contains "to 4.12."))
        (fact "has foods on menu"
              page => (contains "Broileriwrap"))
        (fact "has food allergens on menu"
              page => (contains "(PÄ, SE, SO, V, soijaa, valkosipulia)"))))

(defn render [nodes]
  (apply str (html/emit* nodes)))

(fact "#format-food"
      (fact "No allergens"
            (render (view/format-food {:name "Spam"
                                       :meta {:0 [],
                                              :1 [],
                                              :2 []}}))
            => "Spam")
      (fact "Has allergens"
            (render (view/format-food {:name "Spam"
                                       :meta {:0 ["PÄ" "V"],
                                              :1 [],
                                              :2 []}}))
            => "Spam <span class=\"allergens\">(PÄ, V)</span>")
      (fact "Other ingredient warnings"
            (render (view/format-food {:name "Spam"
                                       :meta {:0 ["PÄ" "V"],
                                              :1 ["valkosipulia"],
                                              :2 []}}))
            => "Spam <span class=\"allergens\">(PÄ, V, valkosipulia)</span>"))
