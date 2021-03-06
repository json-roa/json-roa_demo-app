; Copyright (C) 2015 Dr. Thomas Schank  (DrTom@schank.ch, Thomas.Schank@algocon.ch)

(ns json-roa-demo.json-roa
  (:require
    [clojure.tools.logging :as logging]
    [clout.core :as clout :refer [route-matches route-compile]]
    [ring.util.codec :refer [form-encode]]
    [clojure.walk :refer [keywordize-keys]]
    ))

; #############################################################################

(defn build-message-rel [& [response request match _]]
  (let [id (case (:request-method request)
             (:get :head) (or (:id match) "{id}")
             :post (or (-> response :body :id) "{id}")
             "{id}")]
    {:href (str "/messages/" id)
     :relations {:doc {:href "/docs/index.html#message"
                       :name "API-Doc"}}
     :methods {:delete {}, :get {}}}))

(defn build-root-rel [& [response request match _]]
  {:href "/"
   :name "Root"
   :relations {:docs {:href "/docs/index.html"
                      :name "APP-Docs" }
               :root-doc {:href "/docs/index.html#root"
                          :name "API-Doc"} }})

(defn build-messages-rel [& [response request match _]]
  {:href "/messages/"
   :name "Messages"
   :relations{ :messages-doc {:href "/docs/index.html#messages"
                         :name "API-Doc"} }
   :methods {:get {} :post {}} })

(defn build-users-rel [& [response request match _]]
  {:href "/users/"
   :name "Users"
   :relations{:users-doc {:href "/docs/index.html#users"
                          :name "API-Doc"}}
   :methods {:post {}} })


; #############################################################################

(defn add-root-rel [response & _]
  (assoc-in response
            [:body :_json-roa :relations :root]
            (build-root-rel nil nil nil)
            ))

(defn add-messages-rel [response & _]
  (assoc-in response [:body :_json-roa :relations :messages] (build-messages-rel) ))

(defn add-messages-collection [response & [request _]]
  (let [query-params (-> request keywordize-keys :query-params)
        pageindex (Integer. (or (:page  query-params) 0))
        next-query-params (assoc query-params :page (inc pageindex))
        nextrel {:href (str "/messages/?" (form-encode next-query-params))}
        ids (->> (-> response :body :messages)
                 (map :id))
        rels (->> ids
                  (map (fn [id] [id (build-message-rel response request {:id id})]))
                  (into {}))]
    (-> response
        (assoc-in [:body :_json-roa :collection :relations] rels)
        (assoc-in [:body :_json-roa :collection :next] nextrel)
        )))

(defn add-users-rel [response & _]
  (assoc-in response [:body :_json-roa :relations :users] (build-users-rel)))

(defn add-message-rel [response & [request match _]]
  (assoc-in response
            [:body :_json-roa :relations :message ]
            (build-message-rel nil nil match)))


(defn add-add-self-rel [rel-builder]
  (fn [response & [request match _]]
    (assoc-in response
              [:body :_json-roa :self-relation]
              (rel-builder response request match))))


; #############################################################################

; TODO add root for *
(def get-matchers-and-adders
  [[(route-compile "*")
    [add-root-rel]]
   [(route-compile  "/")
    [add-users-rel
     add-message-rel
     add-messages-rel
     (add-add-self-rel build-root-rel)]]
   [(route-compile  "/messages/")
    [add-messages-collection
     (add-add-self-rel build-messages-rel)]]
   [(route-compile  "/messages/:id")
    [add-messages-rel
     (add-add-self-rel build-message-rel)]]])


; TODO add root for *
(def post-matchers-and-adders
  [
   [(route-compile "*")
    [add-root-rel]]
   [(route-compile  "/messages/")
    [add-messages-rel (add-add-self-rel build-message-rel)]]
   ])

; ##############################################################################

(defn apply-adder [adder response request match]
  (apply adder [response request match])
  )

(defn apply-adders [adders response request match]
  (reduce (fn [res adder]
            (apply-adder adder res request match))
          response
          adders))

(defn apply-matcher-and-adders [response request matcher-and-adders]
  (if-let [match (route-matches (first matcher-and-adders) request)]
    (let [adders (second matcher-and-adders)]
      (apply-adders adders response request match))
    response))


; #############################################################################

(defn add-get-specific [response request]
  (reduce (fn [res matcher-and-adders]
            (apply-matcher-and-adders res request matcher-and-adders))
          response
          get-matchers-and-adders))

(defn add-post-specific [response request]
  (reduce (fn [res matcher-and-adders]
            (apply-matcher-and-adders res request matcher-and-adders))
          response
          post-matchers-and-adders))

(defn add-specific [response request]
  (let [request-method (:request-method request)
        status (:status response)]
    (case request-method
      (:get :head) (case status
                     200 (add-get-specific response request)
                     (add-root-rel response))
      :post (case status
              201 (add-post-specific response request)
              (add-root-rel response)))))



; #############################################################################

(defn add-base [response]
  (assoc response
         :body (assoc (:body response)
                      :_json-roa {:version "1.0.0"
                                  :relations {}})))

(defn handler [request response]
  (let [body (:body response)]
    (if (and body (map? body))
      (-> response
          add-base
          (add-specific request))
      response)))


