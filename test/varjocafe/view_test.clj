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
            => "Spam <span class=\"allergens\">(PÄ, V, valkosipulia)</span>")
      (fact "Additional information"
            (render (view/format-food {:name "Spam"
                                       :meta {:0 ["PÄ" "V"],
                                              :1 ["valkosipulia"],
                                              :2 ["Ilmastovalinta"]}}))
            => "Spam <span class=\"allergens\">(PÄ, V, valkosipulia, Ilmastovalinta)</span>"))

(fact "#format-day-range"
      (fact "Contiguous full week"
            (view/format-day-range ["Ma" "Ti" "Ke" "To" "Pe" "La" "Su"]) => "Ma-Su")
      (fact "Contiguous beginning of week"
            (view/format-day-range ["Ma" "Ti" "Ke" "To" "Pe" "La" false]) => "Ma-La")
      (fact "Contiguous end of week"
            (view/format-day-range [false "Ti" "Ke" "To" "Pe" "La" "Su"]) => "Ti-Su")
      (fact "Contiguous minimum range"
            (view/format-day-range ["Ma" "Ti" false false false false false]) => "Ma-Ti")
      (fact "Multiple contiguous ranges"
            (view/format-day-range ["Ma" "Ti" false "To" "Pe" false false]) => "Ma-Ti, To-Pe")
      (fact "Non-contiguous"
            (view/format-day-range ["Ma" false "Ke" false "Pe" false "Su"]) => "Ma, Ke, Pe, Su")
      (fact "Single day"
            (view/format-day-range ["Ma" false false false false false false]) => "Ma")
      (fact "No days"
            (view/format-day-range [false false false false false false false]) => ""))

(fact "#format-opening-times"
      (fact "Contiguous date ranges are delimited with a line"
            (view/format-opening-times [{:when ["Ma" "Ti" "Ke" "To" "Pe" false false]
                                         :open "10:30", :close "16:00"}])
            => ["Ma-Pe" "10:30-16:00"])
      (fact "Non-contiguous dates are delimited with a comma"
            (view/format-opening-times [{:when ["Ma" false "Ke" false "Pe" false false]
                                         :open "10:30", :close "16:00"}])
            => ["Ma, Ke, Pe" "10:30-16:00"])
      (fact "Opening and closing times may vary by day of week"
            (view/format-opening-times [{:when ["Ma" "Ti" "Ke" "To" false false false]
                                         :open "10:30", :close "16:00"}
                                        {:when ["previous" "previous" "previous" "previous" "Pe" false false]
                                         :open "10:30", :close "15:00"}])
            => ["Ma-To" "10:30-16:00"
                "Pe" "10:30-15:00"]))
