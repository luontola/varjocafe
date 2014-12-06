(ns user
  (:use midje.repl)
  (:require [clojure.repl :refer :all]
            [clojure.pprint :refer [pprint]]
            [clojure.tools.namespace.repl :refer [refresh]]
            [varjocafe.backend :as backend]
            [varjocafe.settings :as settings]))

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

(def backend-local (backend/init-local (:testdata-dir settings/dev-settings)))
(def backend-remote (backend/init-remote (:backend-url settings/dev-settings)))

(defn update-testdata! []
  (backend/refresh backend-local backend-remote))
