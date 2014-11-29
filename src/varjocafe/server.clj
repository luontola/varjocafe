(ns varjocafe.server
  (:gen-class)
  (:require [clojure.tools.logging :as log]
            [compojure.route :as r]
            [compojure.core :as c]
            [org.httpkit.server :as hs]
            [ring.util.response :as rsp]
            [ring.middleware.json :refer [wrap-json-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.force-reload :refer [wrap-force-reload]]
            [lolog.core :refer [wrap-log-request]]
            [varjocafe.settings :as settings]
            [varjocafe.views :as views]))

(defn using-template
  [template & args]
  (-> (rsp/response (apply str (apply template args)))
      (rsp/content-type "text/html")
      (rsp/charset "UTF-8")))

(defn ^:private routes [settings]
  (c/routes
    (c/GET "/status" [] "OK")
    (c/GET "/" [] (using-template views/main-page))
    (r/not-found "Not found")))

(defn wrap-if-dev [next settings handler & args]
  (if (:development-mode settings)
    (apply handler next args)
    next))

(defn app
  "Application without HTTP bindings. Just the Ring stack"
  [settings]
  (->
    (routes settings)
    (wrap-if-dev settings wrap-force-reload ['varjocafe.views])
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

(defn shutdown! [server]
  (log/info "Shutting down server")
  ((:shutdown server))
  (log/info "Server shutdown ok"))

(defn -main [& args]
  (start! settings/defaultsettings))
