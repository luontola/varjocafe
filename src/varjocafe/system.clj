(ns varjocafe.system
  (:gen-class)
  (:import (org.joda.time DateTime))
  (:require [com.stuartsierra.component :as component]
            [varjocafe.core :as core]
            [varjocafe.backend :as backend]
            [varjocafe.server :as server]
            [varjocafe.settings :as settings]
            [varjocafe.updater :as updater]))

(defn init [settings]
  (let [clock (if (:development-mode settings)
                (fn [] (backend/local-updated (:testdata-dir settings)))
                (fn [] (DateTime.)))
        backend (if (:development-mode settings)
                  (backend/init-local (:testdata-dir settings))
                  (backend/init-remote (:backend-url settings)))
        data-provider (core/data-provider backend clock)]
    (component/system-map
      :settings settings
      :database (atom {})
      :server (component/using (server/init)
                               [:database :settings])
      :updater (component/using (updater/init)
                                [:database :data-provider :settings])
      :backend backend
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
