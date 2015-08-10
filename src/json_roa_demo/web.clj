; Copyright (C) 2015 Dr. Thomas Schank  (DrTom@schank.ch, Thomas.Schank@algocon.ch)

(ns json-roa-demo.web
  (:require
    [cider-ci.open-session.cors :as cors]
    [clojure.java.io :as io]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj :refer [defroutes GET PUT POST DELETE ANY]]
    [compojure.handler :refer [site]]
    [compojure.route :as route]
    [drtom.logbug.thrown :as thrown]
    [environ.core :refer [env]]
    [json-roa-demo.resources :as resources]
    [ring.adapter.jetty :as jetty]
    [ring.middleware.json]
    [ring.middleware.resource]
    ))

(defn static-resources-handler [request]
  (ring.middleware.resource/resource-request request ""))

(defn wrap-static-resources-dispatch [default-handler]
  (cpj/routes
    (cpj/ANY "/docs*" request static-resources-handler)
    (cpj/ANY "/api-browser*" request static-resources-handler)
    (cpj/ANY "*" request default-handler)))

(defn wrap-exception [handler]
  (try
    (fn [request] (handler request))
    (catch Exception e
      {:status 500
       :body (thrown/stringify e)})))

(defn wrap-logging [handler]
  (fn [request]
    (let [response (handler request)]
      (when-not (clojure.string/blank? (env :log-requests))
        (logging/info 'wrap-logging 'REQUEST-RESPONSE
                      {:request request :response response}))
      response)))

(defn build-site []
  (-> (resources/build-routes-handler)
      wrap-static-resources-dispatch
      ring.middleware.json/wrap-json-params
      cors/wrap
      site
      wrap-logging))

(def ^:dynamic *server* nil)

(defn start-server [& [port]]
  (when *server*
    (.stop *server*)
    (def ^:dynamic *server* nil))
  (let [port (Integer. (or port (env :port) 5000))]
    (def ^:dynamic *server*
      (jetty/run-jetty (build-site)
                       {:port port :join? false}))))


