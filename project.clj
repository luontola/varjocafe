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
                 [http-kit "2.1.16"]

                 ; HTTP Routing
                 [compojure "1.2.1"]

                 ; HTML Templating
                 [enlive "1.1.5"]

                 ; Date & Time
                 [clj-time "0.8.0"]

                 ; JSON
                 [org.clojure/data.json "0.2.5"]

                 ; Command Line Interface
                 [org.clojure/tools.cli "0.3.1"]]

  :main varjocafe.server

  :profiles {:dev {:dependencies [[midje "1.6.3"]]
                   :plugins      [[lein-midje "3.1.1"]]}})
