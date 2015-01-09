(ns varjocafe.core
  (:import (org.joda.time DateTime LocalDate Days))
  (:require [clojure.pprint :refer [pprint]]
            [clojure.algo.generic.functor :refer [fmap]]
            [clj-time.format :as tf]
            [varjocafe.backend :as backend]
            [clojure.tools.logging :as log]))


; Date parsing

(defn abs-days [days]
  (if (.isLessThan days Days/ZERO)
    (.negated days)
    days))

(defn- distance-in-days [date1 date2]
  (abs-days (Days/daysBetween (.toDateTimeAtStartOfDay date1)
                              (.toDateTimeAtStartOfDay date2))))

(defn- fix-year [today date-without-year]
  (let [this-year (.getYear today)
        possible-years [(inc this-year)
                        this-year
                        (dec this-year)]
        possibilities (map #(.withYear date-without-year %) possible-years)
        closest-possibility (first (sort-by #(distance-in-days % today)
                                            possibilities))]
    closest-possibility))

(def ^:private day-month-formatter (tf/formatter "dd.MM"))
(def ^:private day-month-year-formatter (tf/formatter "dd.MM.yy"))

(defn parse-date [today date-str]
  (and date-str
       (let [original date-str
             date-str (clojure.string/replace date-str #"[,/]" ".")
             parsers [#"\w+ (\d+\.\d+)\.?" #(fix-year today (tf/parse-local-date day-month-formatter %))
                      #"(\d+\.\d+)\.?" #(fix-year today (tf/parse-local-date day-month-formatter %))
                      #"(\d+\.\d+.\d+)" #(tf/parse-local-date day-month-year-formatter %)]
             date (some (fn [[pattern parser]]
                          (let [match (re-matches pattern date-str)]
                            (if match
                              (parser (second match))
                              nil)))
                        (partition 2 parsers))]
         (if date
           date
           (do
             (log/error "Unknown date format, unable to parse:" original)
             original)))))


; Opening time exception parsing

(defn- pre-process-exception [exception]
  (let [{:keys [from]} exception]
    (cond
      (.contains from "-") (let [[from to] (clojure.string/split from #"-")]
                             (assoc exception :from from
                                              :to to))
      :else exception)))

(defn- post-process-exception [exception]
  (when (instance? LocalDate (:from exception))
    (as-> exception $
          (if (instance? LocalDate (:to $))
            $
            (assoc $ :to (:from $)))
          (if (:closed $)
            (select-keys $ [:from :to :closed])
            (select-keys $ [:from :to :open :close])))))

(defn- enrich-exception [today exception]
  (-> exception
      (pre-process-exception)
      (update-in [:from] #(parse-date today %))
      (update-in [:to] #(parse-date today %))
      (post-process-exception)))

(defn- exception? [exception]
  (seq (:from exception)))

(defn enrich-exceptions [today exceptions]
  (->> exceptions
       (filter exception?)
       (map #(enrich-exception today %))
       (remove nil?)))


; Enrich restaurant data

(defn- group-by-date [data today]
  (zipmap (->> (map :date data)
               (map #(parse-date today %)))
          data))

(defn- enrich-restaurant [restaurant backend today]
  (let [details @(backend/get-restaurant backend (:id restaurant))
        details (update-in details [:information :business :exception] #(enrich-exceptions today %))
        details (update-in details [:information :lounas :exception] #(enrich-exceptions today %))
        details (update-in details [:information :bistro :exception] #(enrich-exceptions today %))]
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
  (->> (vals data)
       (map :menu)
       (map keys)
       (flatten)
       (into #{})
       (sort)))

(defn- areacodes [data]
  (->> (vals data)
       (map :areacode)
       (into #{})
       (sort)))

(defn- restaurants-with-areacode [data areacode]
  (->> (vals data)
       (filter #(= areacode (:areacode %)))
       (sort-by :name)))

(defn restaurants-by-area [data areacode-names]
  (for [areacode (areacodes data)]
    {:areacode    areacode
     :name        (areacode-names areacode "???")
     :restaurants (restaurants-with-areacode data areacode)}))
