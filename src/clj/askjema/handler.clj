(ns askjema.handler
  (:require [compojure.core :refer [defroutes GET PUT]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [ring.middleware.edn :refer [wrap-edn-params]]
            [askjema.views :as views]))

(defroutes app-routes
  (GET "/" [] (views/skjema))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app (handler/site app-routes))

(def war-handler
  (-> app
      (wrap-resource "public")
      (wrap-edn-params)
      (wrap-file-info)))