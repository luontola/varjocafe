(ns varjocafe.updater
  (:import (java.util.concurrent Executors ScheduledExecutorService))
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log]))

(defn update-database [old-data data-provider]
  (let [new-data (data-provider)]
    (or new-data old-data)))

(defn make-update-command [database data-provider]
  #(try
    (log/info "Updating...")
    (swap! database update-database data-provider)
    (log/info "Update finished")
    (catch Throwable t
      (log/warn t "Failed to update"))))

(defrecord Updater [scheduler database data-provider settings]
  component/Lifecycle

  (start [this]
    (let [scheduler (Executors/newSingleThreadScheduledExecutor)]
      (.scheduleWithFixedDelay scheduler
                               (make-update-command database data-provider)
                               0
                               (get-in settings [:updater :interval])
                               (get-in settings [:updater :interval-unit]))
      (assoc this :scheduler scheduler)))

  (stop [this]
    (.shutdownNow (:scheduler this))
    this))

(defn init []
  (map->Updater {}))
