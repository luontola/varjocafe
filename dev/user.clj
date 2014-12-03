(ns user
  (:require [clojure.repl :refer :all]
            [clojure.pprint :refer [pprint]]
            [clojure.tools.namespace.repl :refer [refresh]]
            [varjocafe.updater :as updater]
            [varjocafe.settings :as settings])
  (:import (varjocafe.updater RestRestaurantApi LocalRestaurantApi)))

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

(defn update-testdata []
  (let [origin (RestRestaurantApi. (:restaurant-api-url settings/defaultsettings))
        cache (LocalRestaurantApi. (:testdata-dir settings/defaultsettings))]
    (updater/refresh cache origin)))
