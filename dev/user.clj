(ns user
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clojure.repl :refer :all]
            [clojure.pprint :refer [pprint]]
            [clojure.tools.namespace.repl :refer [refresh]]
            [varjocafe.updater :as updater]
            [varjocafe.settings :as settings])
  (:import (org.apache.commons.io FileUtils)
           (varjocafe.updater RestRestaurantApi)))

(defonce ^:private server (atom nil))

(defn ^:private start! []
  {:pre  [(not @server)]
   :post [@server]}
  (require 'varjocafe.server)
  (reset! server ((ns-resolve 'varjocafe.server 'start!)
                   (assoc @(ns-resolve 'varjocafe.settings 'defaultsettings)
                          :development-mode true))))

(defn ^:private shutdown! []
  {:pre  [@server]
   :post [(not @server)]}
  ((ns-resolve 'varjocafe.server 'shutdown!) @server)
  (reset! server nil))

(defn restart! []
  (when @server
    (shutdown!))
  (refresh :after 'user/start!))


; Test Data

(def testdata-dir (io/file "testdata"))

(defn- delete-testdata []
  (FileUtils/deleteDirectory testdata-dir))

(defn- save-testdata [filename data]
  (let [file (io/file testdata-dir filename)]
    (io/make-parents file)
    (spit file (with-out-str (pprint data)))))

(defn- normalized [data]
  (clojure.walk/postwalk
    (fn [form] (if (map? form)
                 (into (sorted-map) form)
                 form))
    data))

(defn update-testdata []
  (let [api (RestRestaurantApi. (:restaurant-api-url settings/defaultsettings))
        index @(updater/get-restaurants api)
        ids (map :id (:data index))
        restaurants (doall (map (fn [id] [id (updater/get-restaurant api id)])
                                ids))]
    (delete-testdata)
    (save-testdata "restaurants.edn" index)
    (log/info "Saved restaurants")
    (doseq [[id restaurant] restaurants]
      (save-testdata (str "restaurant/" id ".edn")
                     (normalized @restaurant))
      (log/info "Saved restaurant" id))
    (log/info "Test data updated")))
