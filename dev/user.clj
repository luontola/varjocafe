(ns user
  (:use midje.repl)
  (:require [clojure.repl :refer :all]
            [clojure.pprint :refer [pprint]]
            [clojure.tools.namespace.repl :refer [refresh]]))

(defonce ^:private system (atom nil))

(defn start! []
  {:pre  [(not @system)]
   :post [@system]}
  (require 'varjocafe.system)
  (let [settings @(ns-resolve 'varjocafe.settings 'dev-settings)
        init (ns-resolve 'varjocafe.system 'init)
        start (ns-resolve 'varjocafe.system 'start!)]
    (reset! system (-> (init settings)
                       (start)))))

(defn stop! []
  {:pre  [@system]
   :post [(not @system)]}
  (let [stop (ns-resolve 'varjocafe.system 'stop!)]
    (stop @system)
    (reset! system nil)))

(defn restart! []
  (when @system
    (stop!))
  (refresh :after 'user/start!))

(defn data []
  {:pre [@system]}
  @(:database @system))


; Test Data

(defn local-backend []
  ((ns-resolve 'varjocafe.backend 'init-local) (:testdata-dir (ns-resolve 'varjocafe.settings 'dev-settings))))

(defn remote-backend []
  ((ns-resolve 'varjocafe.backend 'init-remote) (:backend-url (ns-resolve 'varjocafe.settings 'dev-settings))))

(defn update-testdata! []
  ((ns-resolve 'varjocafe.backend 'refresh) (local-backend) (remote-backend)))
