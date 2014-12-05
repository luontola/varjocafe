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
