(ns varjocafe.view-test
  (:use midje.sweet)
  (:require [varjocafe.view :as view]
            [varjocafe.testdata :as testdata]))

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
              page => (contains "Porsaan grillipihvi"))))
