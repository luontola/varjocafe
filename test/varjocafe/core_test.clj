(ns varjocafe.core-test
  (:use midje.sweet
        varjocafe.testutil)
  (:import (org.joda.time Days LocalDate))
  (:require [clj-time.core :as t]
            [varjocafe.core :as core]
            [varjocafe.settings :as settings]
            [varjocafe.testdata :as testdata]))

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
      (fact "EEE dd.MM format dates"
            (core/parse-date (t/local-date 2014 1 1) "Ke 01.01") => (t/local-date 2014 1 1)
            (core/parse-date (t/local-date 2014 1 1) "To 02.01") => (t/local-date 2014 1 2))
      (fact "EEE dd.MM. format dates"
            (core/parse-date (t/local-date 2014 1 1) "Ke 01.01.") => (t/local-date 2014 1 1)
            (core/parse-date (t/local-date 2014 1 1) "To 02.01.") => (t/local-date 2014 1 2))
      (fact "dd.MM format dates"
            (core/parse-date (t/local-date 2014 1 1) "01.01") => (t/local-date 2014 1 1)
            (core/parse-date (t/local-date 2014 1 1) "02.01") => (t/local-date 2014 1 2))
      (fact "dd.MM. format dates"
            (core/parse-date (t/local-date 2014 1 1) "01.01.") => (t/local-date 2014 1 1)
            (core/parse-date (t/local-date 2014 1 1) "02.01.") => (t/local-date 2014 1 2))
      (fact "d.M format dates"
            (core/parse-date (t/local-date 2014 1 1) "1.1") => (t/local-date 2014 1 1)
            (core/parse-date (t/local-date 2014 1 1) "2.1") => (t/local-date 2014 1 2))
      (fact "d.M. format dates"
            (core/parse-date (t/local-date 2014 1 1) "1.1.") => (t/local-date 2014 1 1)
            (core/parse-date (t/local-date 2014 1 1) "2.1.") => (t/local-date 2014 1 2))
      (fact "Sets an undefined year to the one closest to today"
            (core/parse-date (t/local-date 2014 12 15) "31.12") => (t/local-date 2014 12 31)
            (core/parse-date (t/local-date 2015 1 15) "31.12") => (t/local-date 2014 12 31)
            (core/parse-date (t/local-date 2014 12 15) "01.01") => (t/local-date 2015 1 1)
            (core/parse-date (t/local-date 2015 1 15) "01.01") => (t/local-date 2015 1 1))
      (fact "dd.MM.yyyy format dates"
            (core/parse-date (t/local-date 2014 1 1) "01.01.2014") => (t/local-date 2014 1 1)
            (core/parse-date (t/local-date 2014 1 1) "02.01.2014") => (t/local-date 2014 1 2)
            (core/parse-date (t/local-date 2014 1 1) "01.01.2015") => (t/local-date 2015 1 1))
      (fact "d.M.yyyy format dates"
            (core/parse-date (t/local-date 2014 1 1) "1.1.2014") => (t/local-date 2014 1 1)
            (core/parse-date (t/local-date 2014 1 1) "2.1.2014") => (t/local-date 2014 1 2)
            (core/parse-date (t/local-date 2014 1 1) "1.1.2015") => (t/local-date 2015 1 1))
      (fact "dd.MM.yy format dates"
            (core/parse-date (t/local-date 2014 1 1) "01.01.14") => (t/local-date 2014 1 1)
            (core/parse-date (t/local-date 2014 1 1) "02.01.14") => (t/local-date 2014 1 2)
            (core/parse-date (t/local-date 2014 1 1) "01.01.15") => (t/local-date 2015 1 1))
      (fact "Using , or / instead of . as the separator"
            (core/parse-date (t/local-date 2014 1 1) "02,01,2014") => (t/local-date 2014 1 2)
            (core/parse-date (t/local-date 2014 1 1) "02/01/2014") => (t/local-date 2014 1 2))
      (fact "nil"
            (core/parse-date (t/local-date 2014 1 1) nil) => nil))

(fact "Enriches opening time exceptions"
      (let [today (t/local-date 2014 12 3)]
        (fact "No exceptions"
              (core/enrich-exceptions today [{:from nil, :to nil, :closed false, :open nil, :close nil}]) => []
              (core/enrich-exceptions today [{:from "", :to "", :closed false, :open "", :close ""}]) => []
              (core/enrich-exceptions today [{:from "", :to nil, :closed false, :open nil, :close nil}]) => []
              (core/enrich-exceptions today [{:from "", :to "", :closed false, :open "00:00", :close "00:00"}]) => []
              (core/enrich-exceptions today [{:from "", :to "", :closed false, :open "08:30", :close "15:00"}]) => []
              (core/enrich-exceptions today [{:from "", :to "", :closed false, :open nil, :close nil}
                                             {:from nil, :to nil, :closed false, :open nil, :close nil}]) => []
              (core/enrich-exceptions today []) => []
              (core/enrich-exceptions today nil) => [])

        (fact "Different opening times for a day"
              (core/enrich-exceptions today [{:from "5.12", :to "5.12", :closed false, :open "11:00", :close "13:00"}])
              => [{:from (t/local-date 2014 12 5), :to (t/local-date 2014 12 5), :open "11:00", :close "13:00"}])

        (fact "Closed for a day"
              (core/enrich-exceptions today [{:from "6.12", :to "6.12", :closed true, :open nil, :close nil}])
              => [{:from (t/local-date 2014 12 6), :to (t/local-date 2014 12 6), :closed true}])

        (fact "If closed then opening and closing times don't matter"
              (core/enrich-exceptions today [{:from "6.1", :to "6.1" :closed true, :open "00:00", :close "00:00"}])
              => [{:from (t/local-date 2015 1 6), :to (t/local-date 2015 1 6), :closed true}])

        (fact "Multiple exceptions"
              (core/enrich-exceptions today [{:from "5.12", :to "5.12", :closed false, :open "11:00", :close "13:00"}
                                             {:from "6.12", :to "6.12", :closed true, :open nil, :close nil}])
              => [{:from (t/local-date 2014 12 5), :to (t/local-date 2014 12 5), :open "11:00", :close "13:00"}
                  {:from (t/local-date 2014 12 6), :to (t/local-date 2014 12 6), :closed true}])

        (fact "Range of days"
              (core/enrich-exceptions today [{:from "15.12", :to "19.12", :closed true, :open nil, :close nil}])
              => [{:from (t/local-date 2014 12 15), :to (t/local-date 2014 12 19), :closed true}])

        (fact "Date also mentions the year"
              (core/enrich-exceptions today [{:from "20.12.2014", :to "6.1.2015", :closed true, :open nil, :close nil}])
              => [{:from (t/local-date 2014 12 20), :to (t/local-date 2015 1 6), :closed true}])

        (fact ":to is missing"
              (core/enrich-exceptions today [{:from "06.01.2015", :to nil, :closed true, :open nil, :close nil}])
              => [{:from (t/local-date 2015 1 6), :to (t/local-date 2015 1 6), :closed true}])

        (fact ":from contains both :from and :to"
              (core/enrich-exceptions today [{:from "24.12.2014-28.12.2014", :to nil, :closed true, :open "", :close nil}])
              => [{:from (t/local-date 2014 12 24), :to (t/local-date 2014 12 28), :closed true}])

        (with-silent-logger
          (fact ":from contains freeform text"
                (core/enrich-exceptions today [{:from "Avaamme Ravintolan 7.1.2015", :to nil, :closed false, :open "08:00", :close nil}])
                => []))))

(fact "Enriched restaurant data"
      ; XXX: Date and food constants must be updated when test data is updated.
      (let [data testdata/data]

        (fact "contains restaurants by id"
              (get-in data [1 :name]) => "Metsätalo")

        (fact "contains restaurant information"
              (get-in data [1 :information :address]) => "Fabianinkatu 39")

        (fact "contains menu by date"
              (get-in data [1 :menu (t/local-date 2014 12 1)]) => (contains {:date "Ma 01.12"})
              (get-in data [1 :menu (t/local-date 2014 12 1) :data 0]) => (contains {:name "Luumusmoothie"}))

        (fact "contains enriched opening time exceptions"
              #_(doseq [key (keys data)]
                (prn key (get-in data [key :information :business :exception])))
              (first (get-in data [32 :information :business :exception])) => (contains {:from (t/local-date 2014 12 5)}))

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
