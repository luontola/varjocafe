(ns varjocafe.server-test
  (:use midje.sweet)
  (:require [varjocafe.server :as server]))

(fact "#wrap-source-maps"
      (let [handler (server/wrap-source-maps (constantly {:headers {}}))]
        (fact "Minified JavaScript file"
              (handler {:uri "/script.min.js"}) => {:headers {"X-SourceMap" "/script.min.map"}})
        (fact "Regular JavaScript file"
              (handler {:uri "/script.js"}) => {:headers {}})
        (fact "Regular file"
              (handler {:uri "/file"}) => {:headers {}})
        (fact "URI missing"
              (handler {}) => {:headers {}})))
