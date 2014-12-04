(ns user
  (:require [clojure.repl :refer :all]
            [clojure.pprint :refer [pprint]]
            [clojure.tools.namespace.repl :refer [refresh]]
            [varjocafe.updater :as updater]
            [varjocafe.settings :as settings])
  (:import (varjocafe.updater RestRestaurantApi LocalRestaurantApi)))

(defonce ^:private system (atom nil))

(defn start! []
  {:pre  [(not @system)]
   :post [@system]}
  (require 'varjocafe.system)
  (let [defaultsettings @(ns-resolve 'varjocafe.settings 'defaultsettings)
        settings (assoc defaultsettings
                        :development-mode true)
        init (ns-resolve 'varjocafe.system 'init)
        start (ns-resolve 'varjocafe.system 'start!)]
    (reset! system (-> (init settings)
                       (start)))))

(defn stop! []
  {:pre  [@system]
   :post [(not @system)]}
  (let [stop (ns-resolve 'varjocafe.system 'stop!)]
    (stop @system)
    (reset! system nil)))

(defn restart! []
  (when @system
    (stop!))
  (refresh :after 'user/start!))


; Test Data

(def rest-api (RestRestaurantApi. (:restaurant-api-url settings/defaultsettings)))
(def local-api (LocalRestaurantApi. (:testdata-dir settings/defaultsettings)))

(defn update-testdata []
  (updater/refresh local-api rest-api))
