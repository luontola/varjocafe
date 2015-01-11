(ns varjocafe.settings-test
  (:use midje.sweet
        varjocafe.testutil)
  (:require [varjocafe.settings :as settings]
            [clojure.java.io :as io])
  (:import (java.util.concurrent TimeUnit)))

(fact "#read-properties-file"
      (with-silent-logger
        (settings/read-properties-file (io/file "no-such-file"))) => {}
      (settings/read-properties-file (io/resource "varjocafe/settings_test.properties")) => {"foo" "bar"})

(fact "#dotkeys->tree"
      (fact "flat structures"
            (settings/dotkeys->tree {}) => {}
            (settings/dotkeys->tree {"k" "v"}) => {:k "v"}
            (settings/dotkeys->tree {:k "v"}) => {:k "v"}
            (settings/dotkeys->tree {:k1 "v1", :k2 "v2"}) => {:k1 "v1", :k2 "v2"})
      (fact "hierarchial structures"
            (settings/dotkeys->tree {"parent.k" "v"}) => {:parent {:k "v"}}
            (settings/dotkeys->tree {:parent.k "v"}) => {:parent {:k "v"}}
            (settings/dotkeys->tree {:parent.k1 "v1", :parent.k2 "v2"}) => {:parent {:k1 "v1", :k2 "v2"}})
      (fact "merges with defaults"
            (fact "flat"
                  (settings/dotkeys->tree {} {:a "default"}) => {:a "default"}
                  (settings/dotkeys->tree {"a" "override"} {:a "default"}) => {:a "override"}
                  (settings/dotkeys->tree {"b" "added"} {:a "default"}) => {:a "default", :b "added"})
            (fact "hierarchial"
                  (settings/dotkeys->tree {} {:a {:b "default"}}) => {:a {:b "default"}}
                  (settings/dotkeys->tree {"a.b" "override"} {:a {:b "default"}}) => {:a {:b "override"}}
                  (settings/dotkeys->tree {"a.c" "added"} {:a {:b "default"}}) => {:a {:b "default", :c "added"}})))

(fact "#merge-with-defaults"
      (fact "Coerces to int"
            (-> {"server.port" "8081"}
                (settings/merge-with-defaults settings/default-settings))
            => (contains {:server (contains {:port 8081})}))
      (fact "Coerces to boolean"
            (-> {"development-mode" "true"}
                (settings/merge-with-defaults settings/default-settings))
            => (contains {:development-mode true}))
      (fact "Coerces to TimeUnit"
            (-> {"updater.interval-unit" "SECONDS"}
                (settings/merge-with-defaults settings/default-settings))
            => (contains {:updater (contains {:interval-unit TimeUnit/SECONDS})}))
      (fact "Throws up if settings are not valid"
            (-> {"foo" "bar"}
                (settings/merge-with-defaults settings/default-settings))
            => (throws IllegalArgumentException "Invalid settings: {:foo disallowed-key}")))

(fact "Validate schemas"
      (fact "default-settings"
            (settings/validate settings/default-settings) => truthy)
      (fact "dev-settings"
            (settings/validate settings/dev-settings) => truthy))
