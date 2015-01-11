(ns varjocafe.settings
  (:import (java.util.concurrent TimeUnit)
           (java.io FileNotFoundException)
           (java.util Properties))
  (:require [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [schema.core :as s]
            [schema.coerce :as sc]))

; Schema

(def Settings
  {:backend-url      s/Str
   :server           {:port     s/Int
                      :base-url s/Str}
   :updater          {:interval      s/Int
                      :interval-unit (apply s/enum (TimeUnit/values))}
   :google-analytics {(s/optional-key
                        :tracking-id) s/Str}
   :areacode-names   {s/Int s/Str}
   :development-mode s/Bool
   (s/optional-key
     :testdata-dir)  s/Str})

(defn validate [settings] (s/validate Settings settings))


; Defaults

(def default-settings
  {:backend-url      "http://messi.hyyravintolat.fi/publicapi"
   :server           {:port     8080
                      :base-url ""}
   :updater          {:interval      30
                      :interval-unit TimeUnit/MINUTES}
   :google-analytics {}
   :areacode-names   {1 "Keskusta"
                      2 "Kumpula"
                      3 "Meilahti"
                      5 "Viikki"
                      6 "Metropolia"}
   :development-mode false})

(def dev-settings
  (assoc default-settings
    :development-mode true
    :testdata-dir "testdata"))


; External Configuration

(defn read-properties-file [path]
  (try
    (with-open [in (io/reader path)]
      (doto
        (Properties.)
        (.load in)))
    (catch FileNotFoundException _
      (log/info "Configuration file" path "not found. Using defaults")
      {})))

(defn dotkeys->tree
  ([m] (dotkeys->tree m {}))
  ([m defaults]
    (reduce #(let [[k v] %2
                   path (map keyword (string/split (name k) #"\."))]
              (assoc-in %1 path v))
            defaults
            m)))

(defn- coerce-settings [settings]
  ((sc/coercer Settings sc/string-coercion-matcher) settings))

(defn merge-with-defaults [settings defaults]
  (-> settings
      (dotkeys->tree defaults)
      (coerce-settings)))

(defn read-configuration [defaults]
  (-> (read-properties-file "varjocafe.properties")
      (merge-with-defaults defaults)))
