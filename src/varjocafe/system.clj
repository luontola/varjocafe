(ns varjocafe.system
  (:gen-class)
  (:require [com.stuartsierra.component :as component]
            [varjocafe.settings :as settings]
            [varjocafe.server :as server])
  (:import (varjocafe.updater LocalRestaurantApi RestRestaurantApi)))

(defn init [settings]
  (component/system-map
    :settings settings
    :server (component/using (server/init)
                             [:settings])
    :restaurant-api (if (:development-mode settings)
                      (LocalRestaurantApi. (:testdata-dir settings))
                      (RestRestaurantApi. (:restaurant-api-url settings)))))

(defn start! [system]
  (component/start system))

(defn stop! [system]
  (component/stop system))

(defn -main [& args]
  (->
    (settings/read-configuration settings/defaultsettings)
    (init)
    (start!)))
