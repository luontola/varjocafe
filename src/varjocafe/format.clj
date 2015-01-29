(ns varjocafe.format
  (:import (java.util Locale)
           (org.joda.time.format DateTimeFormat DateTimeFormatter))
  (:require [net.cgrand.enlive-html :as html]
            [clojure.string :as string]
            [varjocafe.core :as core]))

(def ^:private date-formatter (-> (DateTimeFormat/forPattern "E d.M.")
                                  (.withLocale (Locale/forLanguageTag "fi"))))

(defn date [date]
  (.print date-formatter date))

(defn restaurant-address [restaurant]
  (str (:address (:information restaurant))
       ", "
       (:city (:information restaurant))))


; Opening times

(defn- day-set? [date]
  date)

(defn- comma-delimited [prefix suffix]
  (if (empty? suffix)
    prefix
    (str prefix ", " suffix)))

(defn- dash-delimited [contiguous]
  (if (= 1 (count contiguous))
    (first contiguous)
    (str (first contiguous) "-" (last contiguous))))

(defn day-range [days]
  (cond
    (empty? days) ""
    (day-set? (first days)) (comma-delimited
                              (dash-delimited (take-while day-set? days))
                              (day-range (drop-while day-set? days)))
    :else (recur (rest days))))

(defn time-range [open close]
  (when (not-any? empty? [open close])
    (str open "-" close)))

(defn- opening-time [{:keys [when open close]}]
  (let [days (day-range when)
        times (time-range open close)]
    (if (or (empty? days)
            (empty? times))
      nil
      [days times])))

(defn opening-times [schedule]
  (->> schedule
       (map opening-time)
       (remove empty?)
       (flatten)))

(defn- br-delimited [rows]
  (reduce (fn
            ([] nil)
            ([a b] (html/html a [:br] b)))
          rows))

(defn- opening-time-html [[dates times]]
  (html/html [:span.dates dates] " " [:span.times times]))

(defn opening-times-html
  ([restaurant category]
    (opening-times-html (get-in restaurant [:information category :regular])))
  ([schedule]
    (->> schedule
         (opening-times)
         (partition 2)
         (map opening-time-html)
         (br-delimited))))

(defn opening-times-title [restaurant category]
  (or (get-in restaurant [:information category :name])
      (core/opening-time-categories category)
      "???"))


; Opening time exceptions

(defn exception [restaurant exception]
  (let [category (:category exception)
        title (opening-times-title restaurant category)]
    (if (:closed exception)
      (case category
        :bistro (str title " suljettu")
        :lounas "Ei lounasta"
        "Ravintola suljettu")
      (str title " " (:open exception) "-" (:close exception)))))


; Food

(defn allergens [food]
  (->> food
       :meta
       (sort-by first)
       vals
       flatten
       (string/join ", ")))
