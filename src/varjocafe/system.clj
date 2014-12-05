(ns varjocafe.system
  (:gen-class)
  (:require [com.stuartsierra.component :as component]
            [clj-time.core :as t]
            [varjocafe.core :as core]
            [varjocafe.restaurants :as restaurants]
            [varjocafe.settings :as settings]
            [varjocafe.server :as server]))

(defn init [settings]
  (let [api (if (:development-mode settings)
              (restaurants/init-local (:testdata-dir settings))
              (restaurants/init-remote (:restaurant-api-url settings)))
        data-provider (core/data-provider api t/now)]
    (component/system-map
      :settings settings
      :database (atom {})
      :server (component/using (server/init)
                               [:settings])
      :restaurant-api api
      :data-provider data-provider)))

(defn start! [system]
  (component/start system))

(defn stop! [system]
  (component/stop system))

(defn -main [& args]
  (->
    (settings/read-configuration settings/defaultsettings)
    (init)
    (start!)))
