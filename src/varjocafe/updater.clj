(ns varjocafe.updater
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [org.httpkit.client :as http]))

(def api-url "http://messi.hyyravintolat.fi/publicapi")

(defn json-body [response]
  (if (= 200 (:status response))
    (json/read-str (:body response) :key-fn keyword)
    (do
      (log/warn "Request failed:" (pr-str response))
      nil)))

(defn get-restaurants []
  (future (json-body @(http/get (str api-url "/restaurants")))))

(defn get-restaurant [id]
  (future (json-body @(http/get (str api-url "/restaurant/" id)))))
