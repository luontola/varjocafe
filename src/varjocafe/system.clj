(ns varjocafe.system
  (:gen-class)
  (:import (org.joda.time DateTime))
  (:require [com.stuartsierra.component :as component]
            [varjocafe.core :as core]
            [varjocafe.backend :as backend]
            [varjocafe.server :as server]
            [varjocafe.settings :as settings]
            [varjocafe.updater :as updater]
            [clojure.tools.logging :as log]))

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
      :clock clock
      :data-provider data-provider
      :database (atom {})
      :server (component/using (server/init)
                               [:database :clock :settings])
      :updater (component/using (updater/init)
                                [:database :data-provider :settings]))))

(defn start! [system]
  (component/start system))

(defn stop! [system]
  (component/stop system))

(defn -main [& args]
  (log/info "Starting up")
  (->
    (settings/read-configuration settings/default-settings)
    (init)
    (start!)))
