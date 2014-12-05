(ns varjocafe.testutil
  (:import (org.slf4j LoggerFactory)
           (ch.qos.logback.classic Logger Level)))

(defmacro with-silent-logger [& body]
  `(let [logger# (LoggerFactory/getLogger Logger/ROOT_LOGGER_NAME)
         level# (.getLevel logger#)]
     (try
       (.setLevel logger# Level/OFF)
       ~@body
       (finally
         (.setLevel logger# level#)))))
