(ns varjocafe.testdata
  (:import (org.joda.time DateTime))
  (:require [varjocafe.core :as core]
            [varjocafe.backend :as backend]
            [varjocafe.settings :as settings]))

(def settings settings/dev-settings)

(def today (.toLocalDate (backend/local-updated (:testdata-dir settings))))

(def data (let [backend (backend/init-local (:testdata-dir settings))]
            (core/data backend today)))
