(ns varjocafe.format
  (:import (java.util Locale)
           (org.joda.time.format DateTimeFormat DateTimeFormatter))
  (:require [net.cgrand.enlive-html :as html]
            [clojure.string :as string]))

(def ^:private date-formatter (-> (DateTimeFormat/forPattern "E d.M.")
                                  (.withLocale (Locale/forLanguageTag "fi"))))

(defn date [date]
  (.print date-formatter date))


; Opening Times

(defn- day-set? [date]
  (cond
    (false? date) false
    (= "previous" date) false
    :else true))

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
    (not (day-set? (first days))) (day-range (rest days))
    :else (comma-delimited
            (dash-delimited (take-while day-set? days))
            (day-range (drop-while day-set? days)))))

(defn time-range [open close]
  (str open "-" close))

(defn- opening-time [{:keys [when open close]}]
  [(day-range when)
   (time-range open close)])

(defn opening-times [spec]
  (->> spec
       (map opening-time)
       flatten))


; Food

(defn allergens [food]
  (let [allergens (->> food
                       :meta
                       (sort-by first)
                       vals
                       flatten)]
    (if (empty? allergens)
      nil
      (html/html " " [:span.allergens (str "(" (string/join ", " allergens) ")")]))))

(defn food [food]
  (html/html (:name food) (allergens food)))
