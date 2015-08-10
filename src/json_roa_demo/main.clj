; Copyright (C) 2015 Dr. Thomas Schank  (DrTom@schank.ch, Thomas.Schank@algocon.ch)

(ns json-roa-demo.main
  (:require
    [clojure.tools.logging :as logging]
    [drtom.logbug.thrown]
    [json-roa-demo.data-json]
    [json-roa-demo.web :as web]
  ))

(defn -main []
  (drtom.logbug.thrown/reset-ns-filter-regex #".*json.roa.demo.*")
  (logging/info "Initializing ...")
  (web/start-server)
  (logging/info "Initialized"))

