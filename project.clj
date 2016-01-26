(defproject json-roa_demo-app  "1.1.2"
  :description "JSON-ROA Demo-App"
  :url "https://github.com/json-roa/json-roa_demo-app"
  :license {:name "MIT" }
  :dependencies [
                 [cheshire "5.5.0"]
                 [cider-ci/clj-auth "5.0.1"]
                 [cider-ci/clj-utils "6.1.1"]
                 [cider-ci/open-session "1.2.0"]
                 [clj-logging-config "1.9.12"]
                 [compojure "1.4.0"]
                 [drtom/logbug "1.3.0"]
                 [environ "1.0.1"]
                 [honeysql "0.6.2"]
                 [json-roa/clj-utils "1.0.0"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail javax.jms/jms com.sun.jdmk/jmxtools com.sun.jmx/jmxri]]
                 [org.clojure/clojure "1.6.0"]
                 [org.clojure/java.jdbc "0.4.2"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.slf4j/slf4j-log4j12 "1.7.14"]
                 [pg-types "2.1.2"]
                 [ring-basic-authentication "1.0.5"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [ring/ring-json "0.4.0"]
                 ]
  :min-lein-version "2.0.0"
  :plugins [
            [environ/environ.lein "0.2.1"]
            ]
  :hooks [environ.leiningen.hooks]
  :uberjar-name "clojure-getting-started-standalone.jar"
  :profiles {:production {:env {:production true}}}
  :aot [json-roa-demo.main]
  :main json-roa-demo.main
  :ring {:handler json-roa-demo.web/handler}
  )
