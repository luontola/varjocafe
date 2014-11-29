(ns varjocafe.server
  (:gen-class)
  (:use [clojure.tools.cli :only [cli]]))

(defn start! [options]
  (println "TODO"))

(defn -main [& args]
  (let [[options args banner] (cli args
                                   ["--port" "Port for the HTTP server to listen to" :default 8080 :parse-fn #(Integer. %)]
                                   ["--help" "Show this help" :flag true])]
    (when (:help options)
      (println banner)
      (System/exit 0))
    (start! options)))

