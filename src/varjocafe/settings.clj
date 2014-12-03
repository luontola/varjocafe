(ns varjocafe.settings)

(def defaultsettings
  {:restaurant-api-url "http://messi.hyyravintolat.fi/publicapi"
   :server             {:port     8080
                        :base-url ""}
   :development-mode   false})

(defn read-configuration [defaults]
  defaults)                                                 ; TODO: allow overriding the port etc.
