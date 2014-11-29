(ns user
  (:require [clojure.repl :refer :all]
            [clojure.pprint :refer [pprint]]
            [clojure.tools.namespace.repl :as nsr]))

(defonce ^:private server (atom nil))

(defn ^:private start! []
  {:pre  [(not @server)]
   :post [@server]}
  (require 'varjocafe.server)
  (reset! server ((ns-resolve 'varjocafe.server 'start!)
                   (assoc @(ns-resolve 'varjocafe.settings 'defaultsettings)
                          :development-mode true))))

(defn ^:private shutdown! []
  {:pre  [@server]
   :post [(not @server)]}
  ((ns-resolve 'varjocafe.server 'shutdown) @server)
  (reset! server nil))

(defn restart! []
  (when @server
    (shutdown!))
  (nsr/refresh :after 'user/start!))
