(ns varjocafe.server
  (:import (org.joda.time DateTime))
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
            [varjocafe.view :as view]
            [clojure.string :as string]))

(defn- using-template
  [template & args]
  (-> (rsp/response (apply str (apply template args)))
      (rsp/content-type "text/html")
      (rsp/charset "UTF-8")))

(defn- main-page [date-selector database clock settings]
  (let [today (.toLocalDate (clock))]
    (using-template view/main-page
                    @database
                    (date-selector today)
                    today
                    settings)))

(defn- routes [database clock settings]
  (c/routes
    (c/GET "/" [] (main-page view/today-and-tomorrow database clock settings))
    (c/GET "/week" [] (main-page view/this-week database clock settings))
    (c/GET "/next-week" [] (main-page view/next-week database clock settings))
    (c/GET "/status" [] "OK")
    (r/not-found "Not found")))

(defn- wrap-if-dev [next settings handler & args]
  (if (:development-mode settings)
    (apply handler next args)
    next))

(defn wrap-source-maps [handler]
  (fn [request]
    (let [response (handler request)
          uri (:uri request)
          minified-ext #"\.min\.js$"]
      (if (and uri
               (re-find minified-ext uri))
        (assoc-in response [:headers "X-SourceMap"] (string/replace-first uri minified-ext ".min.map"))
        response))))

(defn ring-stack [database clock settings]
  (->
    (routes database clock settings)
    (wrap-if-dev settings wrap-force-reload ['varjocafe.view])
    wrap-keyword-params
    wrap-json-params
    wrap-params
    (wrap-resource "public")
    wrap-source-maps
    wrap-content-type
    wrap-log-request))

(defrecord Server [shutdown database clock settings]
  component/Lifecycle

  (start [component]
    (let [port (get-in settings [:server :port])
          shutdown (hs/run-server (ring-stack database clock settings) {:port port})]
      (log/info "Listening on port" port)
      (assoc component :shutdown shutdown)))

  (stop [component]
    (log/info "Shutting down")
    ((:shutdown component))))

(defn init []
  (map->Server {}))
