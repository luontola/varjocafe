(ns varjocafe.settings-test
  (:use midje.sweet
        varjocafe.testutil)
  (:require [varjocafe.settings :as settings]
            [clojure.java.io :as io]))

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
            (settings/dotkeys->tree {:parent.k1 "v1", :parent.k2 "v2"}) => {:parent {:k1 "v1", :k2 "v2"}}))
