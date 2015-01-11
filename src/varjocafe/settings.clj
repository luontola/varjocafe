(ns varjocafe.settings
  (:import (java.util.concurrent TimeUnit)
           (java.io FileNotFoundException)
           (java.util Properties))
  (:require [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [clojure.string :as string]))

(def default-settings
  {:backend-url      "http://messi.hyyravintolat.fi/publicapi"
   :server           {:port     8080
                      :base-url ""}
   :updater          {:interval      30
                      :interval-unit TimeUnit/MINUTES}
   :development-mode false
   :areacode-names   {1 "Keskusta"
                      2 "Kumpula"
                      3 "Meilahti"
                      5 "Viikki"
                      6 "Metropolia"}
   ; TODO: specify in external configuration
   :google-analytics {:tracking-id "UA-5984051-8"}})

(def dev-settings
  (assoc default-settings
    :testdata-dir "testdata"
    :development-mode true))


(defn read-properties-file [path]
  (try
    (with-open [in (io/reader path)]
      (doto
        (Properties.)
        (.load in)))
    (catch FileNotFoundException _
      (log/info "Configuration file" path "not found. Using defaults")
      {})))

(defn dotkeys->tree [m]
  (reduce #(let [[k v] %2
                 path (map keyword (string/split (name k) #"\."))]
            (assoc-in %1 path v))
          {}
          m))

(defn read-configuration [defaults]
  ; TODO: allow overriding the port etc.
  defaults)
