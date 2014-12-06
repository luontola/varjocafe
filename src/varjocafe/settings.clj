(ns varjocafe.settings
  (:import (java.util.concurrent TimeUnit)))

(def default-settings
  {:backend-url      "http://messi.hyyravintolat.fi/publicapi"
   :server           {:port     8080
                      :base-url ""}
   :updater          {:interval      30
                      :interval-unit TimeUnit/MINUTES}
   :development-mode false
   :areacode-names   {1 "Keskusta"
                      2 "Kumpula"
                      3 "Meilahti"
                      5 "Viikki"
                      6 "Metropolia"}})

(def dev-settings
  (assoc default-settings
         :testdata-dir "testdata"
         :development-mode true))

(defn read-configuration [defaults]
  defaults)                                                 ; TODO: allow overriding the port etc.
