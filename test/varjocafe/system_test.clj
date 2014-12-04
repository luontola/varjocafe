(ns varjocafe.system-test
  (:use midje.sweet)
  (:require [org.httpkit.client :as http]
            [varjocafe.system :as system]
            [varjocafe.settings :as settings]))

(defn with-system [f]
  (let [port 8082
        system (-> settings/defaultsettings
                   (assoc-in [:server :port] port)
                   (system/init)
                   (system/start!))]
    (try
      (f port)
      (finally
        (system/stop! system)))))

(fact "Starts up an HTTP server"
      (with-system
        (fn [port]
          @(http/get (str "http://localhost:" port)) => (contains {:status 200}))))
