; Copyright (C) 2015 Dr. Thomas Schank  (DrTom@schank.ch, Thomas.Schank@algocon.ch)

(ns json-roa-demo.resources.messages
  (:require 
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [clojure.walk :refer [keywordize-keys]]
    [clojure.walk :refer [keywordize-keys]]
    [compojure.core :as cpj]
    [compojure.handler :as cpj.handler]
    [honeysql.core :as hc]
    [honeysql.helpers :as hh]
    [json-roa-demo.data-json]
    [json-roa-demo.rdbms :refer [get-db]]
    [pg-types.all]
    ))


(defn- create [request]
  (logging/debug 'create request)
  (let [params (-> request :params keywordize-keys)
        user (:authenticated_user request)
        row {:login (:login user) 
             :message (-> request :json-params :message)}]
    (logging/debug 'row row)
    (try 
      (let [res (first (jdbc/insert! (get-db) :messages row))]
        {:status 201
         :body res})
      (catch org.postgresql.util.PSQLException e
        {:status 422
         :body {:message (str e)}}))))


(def ^:private index-base-query 
  (-> (hh/select :messages.id)
      (hh/from :messages)
      (hh/order-by [:messages.created_at :desc])
      (hh/offset 0)
      (hh/limit 10)
      ))

(defn- index [request]
  (let 
    [page-index (Integer. (or (-> request keywordize-keys :query-params :page) 0))
     query (-> index-base-query
               (hh/offset (* 10 page-index))
               hc/format)
     messages (jdbc/query (get-db) query)]
    (logging/debug 'query query)
    {:status 200
     :body{ :messages messages}}))

(defn- delete [request]
  (let [id (-> request :route-params :id)
        message (first (jdbc/query (get-db) ["SELECT * FROM messages WHERE id = ?", id]))
        login (-> request :authenticated_user :login)]
    (if-not (= login (:login message))
      {:status 403
       :body {:error "Only creators may delete their own messages!"}}
      (do (jdbc/delete! (get-db) :messages ["id = ?", id])
        {:status 204
         :body {}}))))


(defn- show [request]
  (logging/debug 'show request)
  (let [message (first (jdbc/query 
                         (get-db)
                         ["SELECT * FROM messages where id = ?" 
                          (-> request :params :id)]))]
    (if message
      {:status 200
       :body message}
      {:status 404 })))


(defn routes [handler]
  (cpj/routes
    (cpj/GET "/messages/:id" request #'show)
    (cpj/DELETE "/messages/:id" request #'delete)
    (cpj/GET "/messages/" _ #'index)
    (cpj/POST "/messages/" _ #'create)
    (cpj/ANY "*" _ handler)
    ))

