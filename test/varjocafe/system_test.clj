(ns varjocafe.system-test
  (:use midje.sweet)
  (:require [org.httpkit.client :as http]
            [varjocafe.system :as system]
            [varjocafe.settings :as settings]))

(defn with-system [f]
  (let [port 8082
        system (-> settings/dev-settings
                   (assoc-in [:server :port] port)
                   (system/init)
                   (system/start!))]
    (try
      (f system port)
      (finally
        (system/stop! system)))))

(fact "Starts up an HTTP server"
      (with-system
        (fn [system port]
          @(http/get (str "http://localhost:" port)) => (contains {:status 200}))))

(fact :slow "Updates the database periodically"
      (with-system
        (fn [system port]
          ; TODO: no sleep
          (Thread/sleep 1000)
          @(:database system) => (contains {1 anything}))))
