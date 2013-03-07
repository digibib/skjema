(ns askjema.handler
  (:require [compojure.core :refer [defroutes GET PUT POST]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [ring.middleware.edn :refer [wrap-edn-params]]
            [askjema.views :as views]
            [askjema.sparql :as sparql])
  (:import java.net.URI))

(defn generate-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body (pr-str data)})

(defroutes app-routes
  (GET "/" [] (views/skjema))
  (POST "/review" [uri] (generate-response
                          (-> (sparql/fetch (URI. uri)) sparql/solutions)))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app (handler/site app-routes))

(def war-handler
  (-> app
      (wrap-resource "public")
      (wrap-edn-params)
      (wrap-file-info)))