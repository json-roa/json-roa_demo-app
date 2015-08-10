; Copyright (C) 2015 Dr. Thomas Schank  (DrTom@schank.ch, Thomas.Schank@algocon.ch)

(ns json-roa-demo.resources
  (:require
    [clojure.tools.logging :as logging]
    [clojure.walk :refer [keywordize-keys]]
    [compojure.core :as cpj]
    [compojure.handler :as cpj.handler]
    [compojure.route :as cpj.route]
    [compojure.route :as route]
    [drtom.logbug.thrown :as thrown]
    [json-roa-demo.basic-auth :as basic-auth]
    [json-roa-demo.json-roa :as json-roa]
    [json-roa-demo.resources.messages :as messages]
    [json-roa-demo.resources.users :as users]
    [json-roa.ring-middleware.request :as json-roa_request]
    [json-roa.ring-middleware.response :as json-roa_response]
    [pg-types.all]
    [ring.middleware.json]
    [ring.util.response :refer [header]]
    ))


(def ^:private dead-end-handler
  (cpj/routes
    (cpj/GET "*" _ {:status 404 :body {:message "404 NOT FOUND"}})
    (cpj/ANY "*" _ {:status 501 :body {:message "501 NOT IMPLEMENTED"}})
    ))

(defn index [request]
  {:status 200
   :body {:message ["Welcome to the JSON-ROA Demo."
                    "Visit /docs/index.html for more information about this application."
                    ]
          :project (let [project (-> "project.clj" slurp read-string)]
                     {:name (nth project 1)
                      :version  (nth project 2)})
          }})


(defn wrap-keywordize-request [handler]
  (fn [request]
    (-> request keywordize-keys handler)))

(defn wrap-server-errror [handler]
  (fn [request]
    (try (handler request)
         (catch Exception e
           (logging/warn (thrown/stringify e))
           {:status 500
            :body {:error (thrown/stringify e)}}))))


(defn wrap-authenticated-routes [handler]
  (cpj/routes
    (cpj/ANY "/messages/*" _
             (basic-auth/wrap
               (wrap-server-errror
                 (messages/routes dead-end-handler))))
    (cpj/ANY "*" _ handler)))


(defn wrap-public-routes [handler]
  (cpj/routes
    (cpj/GET "/" _ #'index)
    (cpj/POST "/users/" _ #'users/create)
    (cpj/OPTIONS "/users/" _ (-> {:status 200} (header "Allow" "POST")))
    (cpj/OPTIONS "/messages/" _ (-> {:status 200} (header "Allow" "GET, POST")))
    (cpj/OPTIONS "/messages/:id" _ (-> {:status 200} (header "Allow" "GET, DELETE")))
    (cpj/ANY "*" _ handler)))

(defn build-routes-handler []
  (-> dead-end-handler
      wrap-authenticated-routes
      wrap-public-routes
      wrap-keywordize-request
      (json-roa_request/wrap json-roa/handler)
      json-roa_response/wrap
      ring.middleware.json/wrap-json-response
      ))
