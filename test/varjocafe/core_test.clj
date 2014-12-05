(ns varjocafe.core-test
  (:use midje.sweet)
  (:import (org.joda.time Days))
  (:require [clj-time.core :as t]
            [varjocafe.core :as core]
            [varjocafe.backend :as backend]
            [varjocafe.settings :as settings]))

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
              (get-in data [1 :menu (t/local-date 2014 12 1) :data 0]) => (contains {:name "Luumusmoothie"}))))


; Tests for helper functions

(fact "Parses formatted dates to LocalDate objects"
      (fact "parses DD.MM format dates"
            (core/parse-date "Ke 01.01" (t/local-date 2014 1 1)) => (t/local-date 2014 1 1)
            (core/parse-date "To 02.01" (t/local-date 2014 1 1)) => (t/local-date 2014 1 2))
      (fact "sets the year to the one closest to today"
            (core/parse-date "Ke 31.12" (t/local-date 2014 12 15)) => (t/local-date 2014 12 31)
            (core/parse-date "Ke 31.12" (t/local-date 2015 1 15)) => (t/local-date 2014 12 31)
            (core/parse-date "To 01.01" (t/local-date 2014 12 15)) => (t/local-date 2015 1 1)
            (core/parse-date "To 01.01" (t/local-date 2015 1 15)) => (t/local-date 2015 1 1)))

(fact "#abs-days"
      (core/abs-days (Days/days 0)) => (Days/days 0)
      (core/abs-days (Days/days 1)) => (Days/days 1)
      (core/abs-days (Days/days -1)) => (Days/days 1))
