(ns varjocafe.restaurants-test
  (:use midje.sweet)
  (:require [clj-time.core :as t]
            [varjocafe.restaurants :as r]
            [varjocafe.settings :as settings])
  (:import (org.joda.time Days)))

(def restaurant-api-url (:restaurant-api-url settings/defaultsettings))
(def testdata-dir (:testdata-dir settings/defaultsettings))

(fact :slow "RemoteRestaurantApi"
      (let [api (r/init-remote restaurant-api-url)]

        (fact "downloads restaurants index"
              @(r/get-restaurants api) => (contains {:status "OK"}))

        (fact "downloads restaurant by id"
              @(r/get-restaurant api 1) => (contains {:status "OK"}))))

(fact "LocalRestaurantApi"
      (let [api (r/init-local testdata-dir)]

        (fact "reads restaurants index"
              @(r/get-restaurants api) => (contains {:status "OK"}))

        (fact "reads restaurant by id"
              @(r/get-restaurant api 1) => (contains {:status "OK"}))))
