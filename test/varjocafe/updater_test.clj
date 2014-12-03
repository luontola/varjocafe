(ns varjocafe.updater-test
  (:use midje.sweet)
  (:require [varjocafe.updater :as updater]
            [varjocafe.settings :as settings])
  (:import (varjocafe.updater RestRestaurantApi)))

(fact :slow "RestRestaurantApi"
      (let [api (RestRestaurantApi. (:restaurant-api-url settings/defaultsettings))]

        (fact "downloads restaurants index"
              @(updater/get-restaurants api) => (contains {:status "OK"}))

        (fact :slow "downloads restaurant by id"
              @(updater/get-restaurant api 1) => (contains {:status "OK"}))))
