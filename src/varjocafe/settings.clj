(ns varjocafe.settings
  (:import (java.util.concurrent TimeUnit)
           (java.io FileNotFoundException)
           (java.util Properties))
  (:require [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [schema.core :as schema]
            [schema.coerce :as coerce]
            [schema.utils]))

; Schema

(def Settings
  {:backend-url      schema/Str
   :server           {:port     schema/Int
                      :base-url schema/Str}
   :updater          {:interval      schema/Int
                      :interval-unit TimeUnit}
   :google-analytics {(schema/optional-key
                        :tracking-id) schema/Str}
   :areacode-names   {schema/Int schema/Str}
   :development-mode schema/Bool
   (schema/optional-key
     :testdata-dir)  schema/Str})

(defn validate [settings] (schema/validate Settings settings))


; Defaults

(def default-settings
  {:backend-url      "https://messi.hyyravintolat.fi/publicapi"
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
    :testdata-dir "testdata"
    ;:testdata-dir "testdata-exceptions"
    ))


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

(defn- string->TimeUnit [s]
  (if (string? s)
    (try
      (TimeUnit/valueOf s)
      (catch IllegalArgumentException e
        (schema.utils/error (.getMessage e))))
    s))

(defn- coercion-matcher [schema]
  (or (coerce/string-coercion-matcher schema)
      ({TimeUnit string->TimeUnit} schema)))

(defn- coerce-settings [settings]
  (let [result ((coerce/coercer Settings coercion-matcher) settings)]
    (when (schema.utils/error? result)
      (throw (IllegalArgumentException.
               (str "Invalid settings: " (schema.utils/error-val result)))))
    result))

(defn merge-with-defaults [settings defaults]
  (-> settings
      (dotkeys->tree defaults)
      (coerce-settings)))

(defn read-configuration [defaults]
  (-> (read-properties-file "varjocafe.properties")
      (merge-with-defaults defaults)))
