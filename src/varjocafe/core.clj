(ns varjocafe.core
  (:import (org.joda.time DateTime LocalDate Days))
  (:require [clojure.pprint :refer [pprint]]
            [clojure.algo.generic.functor :refer [fmap]]
            [clj-time.format :as tf]
            [varjocafe.backend :as backend]
            [clojure.tools.logging :as log]))

(def opening-time-categories (array-map :business "Auki"
                                        :lounas "Lounas"
                                        :bistro "Bistro"))

(defn opening-time-category-index [category]
  (.indexOf (keys opening-time-categories)
            (:category category)))

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


; Enrich opening times

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

(defn- normalize-when [when]
  (if (= "previous" when)
    false
    when))

(defn- enrich-regular [regular]
  (as-> regular $
        (update-in $ [:when] #(map normalize-when %))
        (if (and (empty? (:open $))
                 (empty? (:close $)))
          nil
          [$])))

(defn enrich-opening-time [today schedule]
  (as-> schedule $
        (update-in $ [:regular] #(mapcat enrich-regular %))
        (update-in $ [:exception] #(enrich-exceptions today %))
        (if (empty? (:regular $))
          nil
          $)))


; Enrich restaurant data

(defn- group-by-date [data today]
  (zipmap (->> (map :date data)
               (map #(parse-date today %)))
          data))

(defn- enrich-restaurant-details [details today]
  (reduce (fn [details category]
            (update-in details [:information category] #(enrich-opening-time today %)))
          details
          (keys opening-time-categories)))

(defn- enrich-restaurant [restaurant backend today]
  (let [details @(backend/get-restaurant backend (:id restaurant))
        details (enrich-restaurant-details details today)]
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

(defn- exception-in-effect? [exception date]
  (and (<= 0 (compare date (:from exception)))
       (>= 0 (compare date (:to exception)))))

(defn- find-effective-exceptions [information date]
  (mapcat (fn [[category opening-times]]
            (as-> (:exception opening-times) $
                  (filter #(exception-in-effect? % date) $)
                  (first $)
                  (dissoc $ :from :to)
                  (when $ [(assoc $ :category category)])))
          information))

(defn exceptions-for-date [restaurant date]
  (as-> (:information restaurant) $
        (select-keys $ (keys opening-time-categories))
        (find-effective-exceptions $ date)
        (sort-by opening-time-category-index $)))
