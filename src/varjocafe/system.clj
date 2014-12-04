(ns varjocafe.system
  (:gen-class)
  (:require [clojure.tools.logging :as log]
            [varjocafe.settings :as settings]
            [varjocafe.server :as server]))

(defn init [configuration]
  {:settings   configuration
   :stop-hooks nil})

(defn start! [system]
  (server/start! system))

(defn stop! [system]
  (doseq [stop-hook (:stop-hooks system)]
    (try
      (stop-hook)
      (catch Throwable t
        (log/error t "Failure when shutting down"))))
  (log/info "System stopped"))

(defn -main [& args]
  (->
    (settings/read-configuration settings/defaultsettings)
    (init)
    (start!)))
