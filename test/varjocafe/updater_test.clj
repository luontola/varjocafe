(ns varjocafe.updater-test
  (:use midje.sweet)
  (:require [clj-time.core :as t]
            [varjocafe.updater :as updater]
            [varjocafe.settings :as settings])
  (:import (varjocafe.updater RestRestaurantApi LocalRestaurantApi)
           (org.joda.time Days)))

(def restaurant-api-url (:restaurant-api-url settings/defaultsettings))
(def testdata-dir (:testdata-dir settings/defaultsettings))

(fact :slow "RestRestaurantApi"
      (let [api (RestRestaurantApi. restaurant-api-url)]

        (fact "downloads restaurants index"
              @(updater/get-restaurants api) => (contains {:status "OK"}))

        (fact "downloads restaurant by id"
              @(updater/get-restaurant api 1) => (contains {:status "OK"}))))

(fact "LocalRestaurantApi"
      (let [api (LocalRestaurantApi. testdata-dir)]

        (fact "reads restaurants index"
              @(updater/get-restaurants api) => (contains {:status "OK"}))

        (fact "reads restaurant by id"
              @(updater/get-restaurant api 1) => (contains {:status "OK"}))))

(fact "Enriched restaurant data"
      ; XXX: Date and food constants must be updated when test data is updated.
      (let [today (t/local-date 2014 12 3)
            api (LocalRestaurantApi. testdata-dir)
            data (updater/enriched-data api today)]

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
            (updater/parse-date "Ke 01.01" (t/local-date 2014 1 1)) => (t/local-date 2014 1 1)
            (updater/parse-date "To 02.01" (t/local-date 2014 1 1)) => (t/local-date 2014 1 2))
      (fact "sets the year to the one closest to today"
            (updater/parse-date "Ke 31.12" (t/local-date 2014 12 15)) => (t/local-date 2014 12 31)
            (updater/parse-date "Ke 31.12" (t/local-date 2015 1 15)) => (t/local-date 2014 12 31)
            (updater/parse-date "To 01.01" (t/local-date 2014 12 15)) => (t/local-date 2015 1 1)
            (updater/parse-date "To 01.01" (t/local-date 2015 1 15)) => (t/local-date 2015 1 1)))

(fact "#abs-days"
      (updater/abs-days (Days/days 0)) => (Days/days 0)
      (updater/abs-days (Days/days 1)) => (Days/days 1)
      (updater/abs-days (Days/days -1)) => (Days/days 1))
