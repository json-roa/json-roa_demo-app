; Copyright (C) 2015 Dr. Thomas Schank  (DrTom@schank.ch, Thomas.Schank@algocon.ch)

(ns json-roa-demo.main
  (:require 
    [json-roa-demo.web :as web]
    [json-roa-demo.data-json]
  ))

(defn -main []
  (web/start-server))

