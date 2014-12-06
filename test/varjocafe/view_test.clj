(ns varjocafe.view-test
  (:use midje.sweet)
  (:require [varjocafe.core :as core]
            [varjocafe.settings :as settings]
            [varjocafe.backend :as backend]
            [clj-time.core :as t]
            [varjocafe.view :as view]))

(fact "Main page"
      ; XXX: Date and food constants must be updated when test data is updated.
      (let [today (t/local-date 2014 12 3)
            settings settings/dev-settings
            backend (backend/init-local (:testdata-dir settings))
            data (core/data backend today)
            page (apply str (view/main-page data settings))]
        (fact "has area names"
              page => (contains "Kumpula"))
        (fact "has restaurant names"
              page => (contains "Exactum"))
        (fact "has today's menu"
              page => (contains "ke 3.12."))
        (fact "has foods on menu"
              page => (contains "Porsaan grillipihvi"))))
