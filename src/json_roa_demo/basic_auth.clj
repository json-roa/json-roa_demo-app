; Copyright (C) 2015 Dr. Thomas Schank  (DrTom@schank.ch, Thomas.Schank@algocon.ch)

(ns json-roa-demo.basic-auth
  (:require
    [cider-ci.open-session.bcrypt :refer [checkpw!]]
    [clojure.data.codec.base64 :as base64]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [drtom.logbug.thrown :as thrown]
    [json-roa-demo.rdbms :refer [get-db]]
    )
  )

(defn- decode-base64
  [^String string]
  (apply str (map char (base64/decode (.getBytes string)))))

(defn- extract-basic-auth-properties [request]
  (logging/debug 'extract-basic-auth-properties request)
  (let [std-extr-error (ex-info "Failed to extract basic-auth properties from request." {})]
    (try (let [auth-header (-> request :headers :authorization)
               decoded-val (decode-base64 (last (re-find #"^Basic (.*)$" auth-header)))
               [username password] (clojure.string/split (str decoded-val) #":" 2)]
           (logging/debug 'auth-header auth-header)
           (logging/debug 'decoded-val decoded-val)
           (logging/debug 'up [username password])
           (if-not (and username password)
             (throw std-extr-error)
             [username password]))
         (catch Exception _
           (logging/debug _)
           (throw std-extr-error)))))

(defn- get-db-user! [username]
  (or (first (jdbc/query
               (get-db)
               ["SELECT * FROM USERS WHERE login = ?" username]))
      (throw (ex-info "User does not exist." {}))))

(defn- authenticate [request]
  (logging/debug 'authenticate request)
  (let [[username pasword] (extract-basic-auth-properties request)
        db-user (get-db-user! username)]
    (checkpw! pasword (:password_digest db-user))
    (assoc request
           :authenticated_user db-user)))

(defn wrap [handler]
  (fn [request]
    (try (-> request
             authenticate
             handler)
         (catch Exception e
           (logging/warn (thrown/stringify e))
           {:status 401
            :headers {"WWW-Authenticate" (str "Basic realm=\"JSON-ROA DEMO, credentials required.\" " (.getMessage e))}
            :body {}
            }))))



