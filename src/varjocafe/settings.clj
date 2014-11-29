(ns varjocafe.settings)

(def defaultsettings
  {:server           {:port     8080
                      :base-url ""}
   :development-mode false})

(defn read-configuration [defaults]
  defaults)                                                 ; TODO: allow overriding the port etc.
