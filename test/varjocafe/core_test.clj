(ns varjocafe.core-test
  (:use clojure.test)
  (:require [varjocafe.core :as core]))

(deftest hello-test
  (is (= (core/hello "world") "hello world")))
