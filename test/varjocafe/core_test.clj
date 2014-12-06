(ns varjocafe.core-test
  (:use midje.sweet)
  (:import (org.joda.time Days LocalDate))
  (:require [clj-time.core :as t]
            [varjocafe.core :as core]
            [varjocafe.backend :as backend]
            [varjocafe.settings :as settings]))

(defn dates-since [start]
  (cons start (lazy-seq (dates-since (.plusDays start 1)))))

(defn date-range [start end]
  (take-while #(<= (compare % end) 0) (dates-since start)))

(fact "#date-range"
      (date-range (t/local-date 2000 1 1)
                  (t/local-date 2000 1 1)) => [(t/local-date 2000 1 1)]
      (date-range (t/local-date 2000 1 1)
                  (t/local-date 2000 1 3)) => [(t/local-date 2000 1 1)
                                               (t/local-date 2000 1 2)
                                               (t/local-date 2000 1 3)])

(fact "#abs-days"
      (core/abs-days (Days/days 0)) => (Days/days 0)
      (core/abs-days (Days/days 1)) => (Days/days 1)
      (core/abs-days (Days/days -1)) => (Days/days 1))

(fact "Parses formatted dates to LocalDate objects"
      (fact "parses DD.MM format dates"
            (core/parse-date "Ke 01.01" (t/local-date 2014 1 1)) => (t/local-date 2014 1 1)
            (core/parse-date "To 02.01" (t/local-date 2014 1 1)) => (t/local-date 2014 1 2))
      (fact "sets the year to the one closest to today"
            (core/parse-date "Ke 31.12" (t/local-date 2014 12 15)) => (t/local-date 2014 12 31)
            (core/parse-date "Ke 31.12" (t/local-date 2015 1 15)) => (t/local-date 2014 12 31)
            (core/parse-date "To 01.01" (t/local-date 2014 12 15)) => (t/local-date 2015 1 1)
            (core/parse-date "To 01.01" (t/local-date 2015 1 15)) => (t/local-date 2015 1 1)))

(fact "Enriched restaurant data"
      ; XXX: Date and food constants must be updated when test data is updated.
      (let [today (t/local-date 2014 12 3)
            backend (backend/init-local (:testdata-dir settings/dev-settings))
            data (core/data backend today)]

        (fact "contains restaurants by id"
              (get-in data [1 :name]) => "Metsätalo")

        (fact "contains restaurant information"
              (get-in data [1 :information :address]) => "Fabianinkatu 39")

        (fact "contains menu by date"
              (get-in data [1 :menu (t/local-date 2014 12 1)]) => (contains {:date "Ma 01.12"})
              (get-in data [1 :menu (t/local-date 2014 12 1) :data 0]) => (contains {:name "Luumusmoothie"}))

        (fact "get available dates"
              (core/dates data) => (date-range (t/local-date 2014 12 1)
                                               (t/local-date 2014 12 14)))

        (fact "get restaurants grouped by area"
              (let [areadata (core/restaurants-by-area data (:areacode-names settings/dev-settings))]
                (fact "areas are ordered by areacode"
                      areadata => (contains [(contains {:areacode 1, :name "Keskusta"})
                                             (contains {:areacode 2, :name "Kumpula"})]
                                            :gaps-ok))
                (fact "restaurants are ordered by name"
                      (:restaurants (second areadata)) => (contains [(contains {:name "Chemicum"})
                                                                     (contains {:name "Exactum"})
                                                                     (contains {:name "Physicum"})]))
                (fact "unknown areacodes get ??? as name"
                      (core/restaurants-by-area {999 {:areacode 9, :id 999, :name "New Restaurant"}}
                                                (:areacode-names settings/default-settings))
                      => [{:areacode    9
                           :name        "???"
                           :restaurants [{:areacode 9, :id 999, :name "New Restaurant"}]}])))))
