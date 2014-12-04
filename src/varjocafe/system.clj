(ns varjocafe.system
  (:gen-class)
  (:require [com.stuartsierra.component :as component]
            [varjocafe.restaurant-api :as restaurant-api]
            [varjocafe.settings :as settings]
            [varjocafe.server :as server]))

(defn init [settings]
  (component/system-map
    :settings settings
    :database (atom {})
    :server (component/using (server/init)
                             [:settings])
    :restaurant-api (if (:development-mode settings)
                      (restaurant-api/init-local (:testdata-dir settings))
                      (restaurant-api/init-remote (:restaurant-api-url settings)))))

(defn start! [system]
  (component/start system))

(defn stop! [system]
  (component/stop system))

(defn -main [& args]
  (->
    (settings/read-configuration settings/defaultsettings)
    (init)
    (start!)))
