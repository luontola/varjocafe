(ns varjocafe.restaurants
  (:require [clojure.data.json :as json]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [clojure.tools.logging :as log]
            [clojure.algo.generic.functor :refer [fmap]]
            [clj-time.format :as tf]
            [org.httpkit.client :as http])
  (:import (org.apache.commons.io FileUtils)
           (org.joda.time DateTime LocalDate Days)))

(defprotocol RestaurantApi
  (get-restaurants [this])
  (get-restaurant [this id]))


; RemoteRestaurantApi

(defn- json-body [response]
  (if (= 200 (:status response))
    (json/read-str (:body response) :key-fn keyword)
    (do
      (log/warn "Request failed:" (pr-str response))
      nil)))

(deftype RemoteRestaurantApi [base-url]
  RestaurantApi
  (get-restaurants [_] (future (json-body @(http/get (str base-url "/restaurants")))))
  (get-restaurant [_ id] (future (json-body @(http/get (str base-url "/restaurant/" id))))))

(defn init-remote [base-url]
  (RemoteRestaurantApi. base-url))


; LocalRestaurantApi

(defprotocol Cache
  (refresh [this origin]))

(defn- delete-directory [dir]
  (FileUtils/deleteDirectory (io/file dir)))

(defn- write-file [file data]
  (do (io/make-parents file)
      (spit file (with-out-str (pprint data)))))

(defn- normalize-maps [data]
  (clojure.walk/postwalk (fn [form] (if (map? form)
                                      (into (sorted-map) form)
                                      form))
                         data))

(defn- index-file [base-dir] (io/file base-dir "restaurants.edn"))
(defn- restaurant-file [base-dir id] (io/file base-dir "restaurant" (str id ".edn")))

(defn- refresh-cache [origin base-dir]
  (let [index @(get-restaurants origin)
        ids (map :id (:data index))
        restaurants (doall (map (fn [id] [id (get-restaurant origin id)])
                                ids))]
    (delete-directory base-dir)
    (write-file (index-file base-dir)
                (normalize-maps index))
    (log/info "Cached restaurants index")
    (doseq [[id restaurant] restaurants]
      (write-file (restaurant-file base-dir id)
                  (normalize-maps @restaurant))
      (log/info "Cached restaurant" id))
    (log/info "Cache refreshed")))

(deftype LocalRestaurantApi [base-dir]
  RestaurantApi
  (get-restaurants [_] (future (edn/read-string (slurp (index-file base-dir)))))
  (get-restaurant [_ id] (future (edn/read-string (slurp (restaurant-file base-dir id)))))
  Cache
  (refresh [_ origin] (refresh-cache origin base-dir)))

(defn init-local [base-dir]
  (LocalRestaurantApi. base-dir))


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

(defn- enrich-restaurant [restaurant api today]
  (let [details @(get-restaurant api (:id restaurant))]
    (assoc restaurant
           :information (:information details)
           :menu (group-by-date (:data details) today))))

(defn- group-by-restaurant-id [index]
  (fmap first (group-by :id (:data index))))

(defn enriched-data [api today]
  (->> @(get-restaurants api)
       (group-by-restaurant-id)
       (fmap #(enrich-restaurant % api today))))

(defn enriched-data-provider [api clock]
  (fn [] (enriched-data api (.toLocalDate (clock)))))
