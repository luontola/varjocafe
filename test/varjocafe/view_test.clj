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
