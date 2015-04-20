(defproject clojure-getting-started "1.0.0-SNAPSHOT"
  :description "Demo Clojure web app"
  :url "http://clojure-getting-started.herokuapp.com"
  :license {:name "Eclipse Public License v1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 [cheshire "5.4.0"]
                 [cider-ci/clj-auth "3.0.0-beta.5"]
                 [cider-ci/clj-utils "2.13.0"]
                 [cider-ci/open-session "1.1.0-beta.3"]
                 [clj-logging-config "1.9.12"]
                 [compojure "1.3.3"]
                 [environ "1.0.0"]
                 [honeysql "0.5.2"]
                 [json-roa/clj-utils "1.0.0-beta.5"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail javax.jms/jms com.sun.jdmk/jmxtools com.sun.jmx/jmxri]]
                 [org.clojure/clojure "1.6.0"]
                 [org.clojure/java.jdbc "0.3.6"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.postgresql/postgresql "9.3-1102-jdbc4"]
                 [org.slf4j/slf4j-log4j12 "1.7.12"]
                 [pg-types "1.0.0"]
                 [ring-basic-authentication "1.0.5"]
                 [ring/ring-jetty-adapter "1.3.2"]
                 [ring/ring-json "0.3.1"]
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
