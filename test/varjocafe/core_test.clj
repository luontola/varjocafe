(ns varjocafe.core-test
  (:use midje.sweet)
  (:require [varjocafe.core :as core]))

(fact "says hello world"
      (core/hello "world") => "hello world")
