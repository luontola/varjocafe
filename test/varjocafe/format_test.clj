(ns varjocafe.format-test
  (:use midje.sweet)
  (:require [varjocafe.format :as format]
            [net.cgrand.enlive-html :as html]))

(defn render [nodes]
  (apply str (html/emit* nodes)))

(fact "Food format"
      (fact "No allergens"
            (render (format/food {:name "Spam"
                                  :meta {:0 [],
                                         :1 [],
                                         :2 []}}))
            => "Spam")
      (fact "Has allergens"
            (render (format/food {:name "Spam"
                                  :meta {:0 ["PÄ" "V"],
                                         :1 [],
                                         :2 []}}))
            => "Spam <span class=\"allergens\">(PÄ, V)</span>")
      (fact "Other ingredient warnings"
            (render (format/food {:name "Spam"
                                  :meta {:0 ["PÄ" "V"],
                                         :1 ["valkosipulia"],
                                         :2 []}}))
            => "Spam <span class=\"allergens\">(PÄ, V, valkosipulia)</span>")
      (fact "Additional information"
            (render (format/food {:name "Spam"
                                  :meta {:0 ["PÄ" "V"],
                                         :1 ["valkosipulia"],
                                         :2 ["Ilmastovalinta"]}}))
            => "Spam <span class=\"allergens\">(PÄ, V, valkosipulia, Ilmastovalinta)</span>"))

(fact "Day range format"
      (fact "Contiguous full week"
            (format/day-range ["Ma" "Ti" "Ke" "To" "Pe" "La" "Su"]) => "Ma-Su")
      (fact "Contiguous beginning of week"
            (format/day-range ["Ma" "Ti" "Ke" "To" "Pe" "La" false]) => "Ma-La")
      (fact "Contiguous end of week"
            (format/day-range [false "Ti" "Ke" "To" "Pe" "La" "Su"]) => "Ti-Su")
      (fact "Contiguous minimum range"
            (format/day-range ["Ma" "Ti" false false false false false]) => "Ma-Ti")
      (fact "Multiple contiguous ranges"
            (format/day-range ["Ma" "Ti" false "To" "Pe" false false]) => "Ma-Ti, To-Pe")
      (fact "Non-contiguous"
            (format/day-range ["Ma" false "Ke" false "Pe" false "Su"]) => "Ma, Ke, Pe, Su")
      (fact "Single day"
            (format/day-range ["Ma" false false false false false false]) => "Ma")
      (fact "No days"
            (format/day-range [false false false false false false false]) => ""))

(fact "Opening times format"
      (fact "Contiguous date ranges are delimited with a dash"
            (format/opening-times [{:when ["Ma" "Ti" "Ke" "To" "Pe" false false]
                                    :open "10:30", :close "16:00"}])
            => ["Ma-Pe" "10:30-16:00"])
      (fact "Non-contiguous date ranges are delimited with a comma"
            (format/opening-times [{:when ["Ma" false "Ke" false "Pe" false false]
                                    :open "10:30", :close "16:00"}])
            => ["Ma, Ke, Pe" "10:30-16:00"])
      (fact "Opening and closing times may vary by day of week"
            (format/opening-times [{:when ["Ma" "Ti" "Ke" "To" false false false]
                                    :open "10:30", :close "16:00"}
                                   {:when ["previous" "previous" "previous" "previous" "Pe" false false]
                                    :open "10:30", :close "15:00"}])
            => ["Ma-To" "10:30-16:00"
                "Pe" "10:30-15:00"]))
