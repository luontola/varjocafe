(ns user
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clojure.repl :refer :all]
            [clojure.pprint :refer [pprint]]
            [clojure.tools.namespace.repl :refer [refresh]]
            [varjocafe.updater :as updater])
  (:import (java.nio.file Paths Files)
           (org.apache.commons.io FileUtils)))

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

(defn update-testdata []
  (let [index @(updater/get-restaurants)
        ids (map :id (:data index))
        restaurants (doall (map (fn [id] [id (updater/get-restaurant id)])
                                ids))]
    (delete-testdata)
    (save-testdata "restaurants.edn" index)
    (log/info "Saved restaurants")
    (doseq [[id restaurant] restaurants]
      (save-testdata (str "restaurant/" id ".edn") @restaurant)
      (log/info "Saved restaurant" id))
    (log/info "Test data updated")))
