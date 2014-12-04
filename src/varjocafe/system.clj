(ns varjocafe.system
  (:gen-class)
  (:require [com.stuartsierra.component :as component]
            [varjocafe.settings :as settings]
            [varjocafe.server :as server]))

(defn init [settings]
  (component/system-map
    :server (server/init settings)))

(defn start! [system]
  (component/start system))

(defn stop! [system]
  (component/stop system))

(defn -main [& args]
  (->
    (settings/read-configuration settings/defaultsettings)
    (init)
    (start!)))
