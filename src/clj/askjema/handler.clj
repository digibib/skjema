(ns askjema.handler
  (:require [compojure.core :refer [defroutes GET PUT POST]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [ring.middleware.edn :refer [wrap-edn-params]]
            [askjema.sparql :as sparql]
            [clojure.java.io :as io])
  (:import java.net.URI))

(defn generate-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body (pr-str data)})

(defroutes app-routes
  (GET "/" [] (io/resource "public/skjema.html"))
  (POST "/load" [uri]
        (let [res (sparql/fetch (URI. uri))]
          (if (= 200 (res :status))
            (let [data (sparql/solutions res)]
              (if (empty? data)
                (generate-response "Finner ingen anbefaling på den URI-en" 404)
                (generate-response data)))
            (generate-response "Noe gikk galt" (res :status)))))
  (PUT "/save" [uri old updated]
       (let [res (sparql/save (URI. uri) old updated)]
         (if (= 200 (res :status))
           (generate-response "OK. Anbefaling lagret")
           (generate-response "Fikk ikke lagret." (res :status)))))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app (handler/site app-routes))

(def war-handler
  (-> app
      (wrap-resource "public")
      (wrap-edn-params)
      (wrap-file-info)))