(ns varjocafe.server
  (:gen-class)
  (:require [clojure.tools.logging :as log]
            [compojure.route :as r]
            [compojure.core :as c]
            [org.httpkit.server :as hs]
            [ring.middleware.json :refer [wrap-json-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [lolog.core :refer [wrap-log-request]]
            [varjocafe.settings :as settings]))

(defn ^:private routes [settings]
  (c/routes
    (c/GET "/status" [] "OK")
    (c/GET "/" [] "Index page")
    (r/not-found "Not found")))

(defn app
  "Application without HTTP bindings. Just the Ring stack"
  [settings]
  (->
    (routes settings)
    wrap-keyword-params
    wrap-json-params
    (wrap-resource "public/app")
    wrap-params
    wrap-content-type
    wrap-log-request))

(defn start! [defaultsettings]
  (try
    (log/info "Server starting up")
    (let [configuration (settings/read-configuration defaultsettings)
          port (get-in configuration [:server :port])
          shutdown (hs/run-server (app configuration)
                                  {:port port})]
      (log/info "Server started at port" port)
      {:shutdown shutdown})
    (catch Throwable t
      (log/error t "Failed to start server"))))

(defn -main [& args]
  (start! settings/defaultsettings))
