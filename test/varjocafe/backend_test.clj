(ns varjocafe.backend-test
  (:use midje.sweet)
  (:require [varjocafe.backend :as backend]
            [varjocafe.settings :as settings]))

(def backend-url (:backend-url settings/dev-settings))
(def testdata-dir (:testdata-dir settings/dev-settings))

(fact :slow "RemoteBackend"
      (let [backend (backend/init-remote backend-url)]

        (fact "downloads restaurants index"
              @(backend/get-restaurants backend) => (contains {:status "OK"}))

        (fact "downloads restaurant by id"
              @(backend/get-restaurant backend 1) => (contains {:status "OK"}))))

(fact "LocalBackend"
      (let [backend (backend/init-local testdata-dir)]

        (fact "reads restaurants index"
              @(backend/get-restaurants backend) => (contains {:status "OK"}))

        (fact "reads restaurant by id"
              @(backend/get-restaurant backend 1) => (contains {:status "OK"}))))
