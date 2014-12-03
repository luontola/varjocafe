(ns varjocafe.updater-test
  (:use midje.sweet)
  (:require [varjocafe.updater :as updater]))

(fact :slow "downloads restaurants index"
      @(updater/get-restaurants) => (contains {:status "OK"}))

(fact :slow "downloads restaurant by id"
      @(updater/get-restaurant 1) => (contains {:status "OK"}))
