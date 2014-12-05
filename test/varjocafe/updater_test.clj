(ns varjocafe.updater-test
  (:use midje.sweet
        varjocafe.testutil)
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


(defn run-update-command [old-data data-provider]
  (let [database (atom old-data)
        command (updater/make-update-command database data-provider)]
    (command)
    @database))

(fact "Handles failures from data provider gracefully"
      (with-silent-logger
        (fact "no failure; update normally"
              (run-update-command menu-r1-d1 (fn [] menu-r1-d2)) => menu-r1-d2)
        (fact "throws an exception; keep old value"
              (run-update-command menu-r1-d1 (fn [] (throw (Exception. "dummy")))) => menu-r1-d1)
        (fact "returns nil; keep old value"
              (run-update-command menu-r1-d1 (fn [] nil)) => menu-r1-d1)
        (fact "throws InterruptedException; keep old value & keep interrupted status"
              (run-update-command menu-r1-d1 (fn [] (throw (InterruptedException. "dummy")))) => menu-r1-d1
              (Thread/interrupted) => true)))
