(ns varjocafe.core
  (:import (org.joda.time DateTime LocalDate Days))
  (:require [clojure.pprint :refer [pprint]]
            [clojure.algo.generic.functor :refer [fmap]]
            [clj-time.format :as tf]
            [varjocafe.backend :as backend]))


; Enrich restaurant data

(defn abs-days [days]
  (if (.isLessThan days Days/ZERO)
    (.negated days)
    days))

(defn- distance-in-days [date1 date2]
  (abs-days (Days/daysBetween (.toDateTimeAtStartOfDay date1)
                              (.toDateTimeAtStartOfDay date2))))

(defn- fix-year [date-without-year today]
  (let [this-year (.getYear today)
        possible-years [(inc this-year)
                        this-year
                        (dec this-year)]
        possibilities (map #(.withYear date-without-year %) possible-years)
        closest-possibility (first (sort-by #(distance-in-days % today)
                                            possibilities))]
    closest-possibility))

(def ^:private date-formatter (tf/formatter "dd.MM"))

(defn parse-date [date-str today]
  (let [without-weekday (clojure.string/replace-first date-str #"\w+ " "")
        date (tf/parse-local-date date-formatter without-weekday)]
    (fix-year date today)))

(defn- group-by-date [data today]
  (zipmap (->> (map :date data)
               (map #(parse-date % today)))
          data))

(defn- enrich-restaurant [restaurant backend today]
  (let [details @(backend/get-restaurant backend (:id restaurant))]
    (assoc restaurant
           :information (:information details)
           :menu (group-by-date (:data details) today))))

(defn- group-by-restaurant-id [index]
  (fmap first (group-by :id (:data index))))

(defn data [backend today]
  (->> @(backend/get-restaurants backend)
       (group-by-restaurant-id)
       (fmap #(enrich-restaurant % backend today))))

(defn data-provider [backend clock]
  (fn [] (data backend (.toLocalDate (clock)))))


; Accessors

(defn dates [data]
  (->> data
       (vals)
       (map :menu)
       (map keys)
       (flatten)
       (into #{})
       (sort)))
