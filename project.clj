(defproject esport-parser "0.1.0-SNAPSHOT"
  :description "Esports cs:go log event receiver"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [clj-time "0.9.0"] ; required due to bug in lein-ring
                 [metosin/compojure-api "0.21.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.slf4j/slf4j-api "1.7.10"]
                 [ch.qos.logback/logback-classic "1.1.3"]
                 [clojurewerkz/cassaforte "2.0.1"]
                 [clojurewerkz/meltdown "1.1.0"]
                 ]
  :ring {:handler esport-parser.handler/app
         :init esport-parser.handler/init
         :destroy esport-parser.handler/destroy}
  :uberjar-name "server.jar"
  :profiles {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]]
                   :plugins [[lein-ring "0.9.4"]]}})
