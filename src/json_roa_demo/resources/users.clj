; Copyright (C) 2015 Dr. Thomas Schank  (DrTom@schank.ch, Thomas.Schank@algocon.ch)

(ns json-roa-demo.resources.users
  (:require 
    [cider-ci.open-session.bcrypt :refer [hashpw]]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [clojure.walk :refer [keywordize-keys]]
    [json-roa-demo.rdbms :refer [get-db]]
    ))


(defn create [request]
  (logging/debug 'create request)
  (let [params (-> request :params keywordize-keys)
        login (:login params)
        password_digest (-> params :password hashpw)
        row {:login login :password_digest password_digest}
        ]
    (logging/debug 'row row)
    (try 
      (jdbc/insert! (get-db) :users row)
      {:status 201 :body {}}
      (catch org.postgresql.util.PSQLException _
        {:status 422
         :body {:message (str "Login must not exist yet. " 
                "Login must be no longer than 40 chars "
                "and only consist of lowercase letters and numbers.")}}))))

