(ns user
  (:use midje.repl)
  (:require [clojure.repl :refer :all]
            [clojure.pprint :refer [pprint]]
            [clojure.tools.namespace.repl :refer [refresh]]
            [varjocafe.restaurants :as restaurants]
            [varjocafe.settings :as settings]))

(defonce ^:private system (atom nil))

(defn start! []
  {:pre  [(not @system)]
   :post [@system]}
  (require 'varjocafe.system)
  (let [defaultsettings @(ns-resolve 'varjocafe.settings 'defaultsettings)
        settings (assoc defaultsettings
                        :development-mode true)
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


; Test Data

(def local-api (restaurants/init-local (:testdata-dir settings/defaultsettings)))
(def remote-api (restaurants/init-remote (:restaurant-api-url settings/defaultsettings)))

(defn update-testdata []
  (restaurants/refresh local-api remote-api))