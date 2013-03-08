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

  (POST "/load" [uri] (do
                        (println (sparql/load-review (URI. uri)))
                        (generate-response
                          (-> (sparql/fetch (URI. uri)) sparql/solutions))))
  (PUT "/save" [uri old updated] (do
                                   (println (sparql/save-review (URI. uri) old updated))
                                   (sparql/save (URI. uri) old updated)))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app (handler/site app-routes))

(def war-handler
  (-> app
      (wrap-resource "public")
      (wrap-edn-params)
      (wrap-file-info)))