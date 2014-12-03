(ns varjocafe.updater-test
  (:use midje.sweet)
  (:require [varjocafe.updater :as updater]
            [varjocafe.settings :as settings])
  (:import (varjocafe.updater RestRestaurantApi LocalRestaurantApi)))

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
