(ns varjocafe.updater
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [org.httpkit.client :as http]))

(defn- json-body [response]
  (if (= 200 (:status response))
    (json/read-str (:body response) :key-fn keyword)
    (do
      (log/warn "Request failed:" (pr-str response))
      nil)))

(defprotocol RestaurantApi
  (get-restaurants [this])
  (get-restaurant [this id]))

(deftype RestRestaurantApi [base-url]
  RestaurantApi
  (get-restaurants [_] (future (json-body @(http/get (str base-url "/restaurants")))))
  (get-restaurant [_ id] (future (json-body @(http/get (str base-url "/restaurant/" id))))))
