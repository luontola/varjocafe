(ns varjocafe.system
  (:gen-class)
  (:require [com.stuartsierra.component :as component]
            [clj-time.core :as t]
            [varjocafe.core :as core]
            [varjocafe.backend :as restaurants]
            [varjocafe.server :as server]
            [varjocafe.settings :as settings]
            [varjocafe.updater :as updater]))

(defn init [settings]
  (let [api (if (:development-mode settings)
              (restaurants/init-local (:testdata-dir settings))
              (restaurants/init-remote (:restaurant-api-url settings)))
        data-provider (core/data-provider api t/now)]
    (component/system-map
      :settings settings
      :database (atom {})
      :server (component/using (server/init)
                               [:database :settings])
      :updater (component/using (updater/init)
                                [:database :data-provider :settings])
      :restaurant-api api
      :data-provider data-provider)))

(defn start! [system]
  (component/start system))

(defn stop! [system]
  (component/stop system))

(defn -main [& args]
  (->
    (settings/read-configuration settings/default-settings)
    (init)
    (start!)))
