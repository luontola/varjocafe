(ns varjocafe.server
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
            [com.stuartsierra.component :as component]
            [lolog.core :refer [wrap-log-request]]
            [varjocafe.views :as views]))

(defn using-template
  [template & args]
  (-> (rsp/response (apply str (apply template args)))
      (rsp/content-type "text/html")
      (rsp/charset "UTF-8")))

(defn ^:private routes []
  (c/routes
    (c/GET "/status" [] "OK")
    (c/GET "/" [] (using-template views/main-page))
    (r/not-found "Not found")))

(defn wrap-if-dev [next settings handler & args]
  (if (:development-mode settings)
    (apply handler next args)
    next))

(defn app [settings]
  (->
    (routes)
    (wrap-if-dev settings wrap-force-reload ['varjocafe.views])
    wrap-keyword-params
    wrap-json-params
    (wrap-resource "public/app")
    wrap-params
    wrap-content-type
    wrap-log-request))

(defrecord Server [settings shutdown]
  component/Lifecycle

  (start [component]
    (let [port (get-in settings [:server :port])
          shutdown (hs/run-server (app settings) {:port port})]
      (log/info "Server listening on port" port)
      (assoc component :shutdown shutdown)))

  (stop [component]
    (log/info "Server shutting down")
    ((:shutdown component))))

(defn init []
  (map->Server {}))
