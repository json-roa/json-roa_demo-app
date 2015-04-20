; Copyright (C) 2015 Dr. Thomas Schank  (DrTom@schank.ch, Thomas.Schank@algocon.ch)

(ns json-roa-demo.pagination
  (:require 
    [honeysql.helpers :as hh]
    ))

(defn- page-number [params]
  (let [page-string (:page params)]
    (if page-string (Integer/parseInt page-string)
      0)))

(defn- compute-offset [params]
  (let [page (page-number params)]
    (* 10 page)))

(defn add-offset [hsql-query params]
  (let [off (compute-offset params)]
    (-> hsql-query
        (hh/offset off)
        (hh/limit 10))))  

(defn increment-page [query-params]
  (let [i-page (page-number query-params)]
    (assoc query-params 
           :page (+ i-page 1))))

