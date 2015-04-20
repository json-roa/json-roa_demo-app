; Copyright (C) 2015 Dr. Thomas Schank  (DrTom@schank.ch, Thomas.Schank@algocon.ch)

(ns json-roa-demo.rdbms
  (:require 
    [clojure.java.jdbc :as jdbc]
    [environ.core :refer [env]]
    ))

(defn get-db []
  (or (env :database-url) 
      "jdbc:postgresql://localhost:5432/json-roa-demo?user=thomas&password=thomas"))

;(jdbc/query (get-db) "SELECT 1 + 1 AS result")
