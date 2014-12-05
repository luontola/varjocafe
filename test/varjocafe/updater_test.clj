(ns varjocafe.updater-test
  (:use midje.sweet)
  (:require [varjocafe.updater :as updater]
            [clj-time.core :as t]))

(def date1 (t/local-date 2014 1 1))
(def date2 (t/local-date 2014 1 2))
(def menu-r1-d1 {1 {:name "Name v1"
                    :menu {date1 "menu 1"}}})
(def menu-r1-d2 {1 {:name "Name v2"
                    :menu {date2 "menu 2"}}})

(fact "Initializes the database"
      (updater/update-database {} (fn [] menu-r1-d1)) => menu-r1-d1)

(fact "Replaces old data with new data"
      (updater/update-database menu-r1-d1 (fn [] menu-r1-d2)) => menu-r1-d2)
