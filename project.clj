(defproject
  varjocafe "2.0.0-SNAPSHOT"
  :description "Mashup of UniCafe's menus"
  :url "http://www.varjocafe.net/"
  :license {:name "Apache License 2.0"
            :url  "http://www.apache.org/licenses/LICENSE-2.0.html"}

  :dependencies [[org.clojure/clojure "1.6.0"]

                 ; HTTP Server
                 [ring/ring-core "1.3.2"]
                 [ring/ring-devel "1.3.2"]
                 [ring/ring-json "0.3.1"]
                 [http-kit "2.1.16"]
                 [ring-middleware-force-reload "0.1.0-SNAPSHOT"]

                 ; HTTP Routing
                 [compojure "1.2.1"]

                 ; HTML Templating
                 [enlive "1.1.5"]

                 ; Date & Time
                 [clj-time "0.8.0"]

                 ; JSON
                 [org.clojure/data.json "0.2.5"]

                 ; Logging
                 [org.clojure/tools.logging "0.3.1"]
                 [ch.qos.logback/logback-classic "1.1.2"]
                 [org.clojars.lokori/lolog "0.1.0"]

                 ; Misc
                 [org.clojure/algo.generic "0.1.2"]
                 [commons-io/commons-io "2.4"]]

  :profiles {:dev     {:source-paths ["dev"]
                       :dependencies [[org.clojure/tools.namespace "0.2.7"]
                                      [midje "1.6.3"]]
                       :plugins      [[lein-midje "3.1.1"]]}
             :uberjar {:aot :all}}
  :repl-options {:init-ns user}

  :main varjocafe.system
  :jar-name "varjocafe.jar"
  :uberjar-name "varjocafe-standalone.jar")
