(ns varjocafe.updater
  (:import (java.util.concurrent Executors ScheduledExecutorService))
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log]))

(defn update-database [database data-provider]
  (data-provider))

(defrecord Updater [scheduler database data-provider settings]
  component/Lifecycle

  (start [this]
    (let [scheduler (Executors/newSingleThreadScheduledExecutor)
          update-command #(do
                           (log/info "Updating...")
                           (swap! database update-database data-provider)
                           (log/info "Update finished"))]
      (.scheduleWithFixedDelay scheduler
                               update-command
                               0
                               (get-in settings [:updater :interval])
                               (get-in settings [:updater :interval-unit]))
      (assoc this :scheduler scheduler)))

  (stop [this]
    (.shutdownNow (:scheduler this))
    this))

(defn init []
  (map->Updater {}))
