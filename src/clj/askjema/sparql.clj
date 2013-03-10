(ns askjema.sparql
  (:refer-clojure :exclude [filter concat group-by max min count])
  (:require [clj-http.client :as client]
            [boutros.matsu.sparql :refer :all]
            [boutros.matsu.core :refer [register-namespaces]]
            [cheshire.core :refer [parse-string]]
            [clj-time.core :refer [now from-time-zone time-zone-for-offset]]
            [clojure.walk :refer [keywordize-keys]]
            [clojure.java.io :as io]
            [askjema.config :refer [config]])
  (:import java.net.URI))

(defn modify [q uri]
  (assoc q :with {:tag "MODIFY" :bounds [" "] :sep " " :content [uri]}))

(register-namespaces {:skos "<http://www.w3.org/2004/02/skos/core#>"
                      :deich "<http://data.deichman.no/>"
                      :foaf "<http://xmlns.com/foaf/0.1/>"
                      :dc "<http://purl.org/dc/terms/>"
                      :rdfs "<http://www.w3.org/2000/01/rdf-schema#>"
                      :fabio "<http://purl.org/spar/fabio/>"
                      :rev "<http://purl.org/stuff/rev#>"
                      :org "<http://www.w3.org/ns/org#>"
                      :xsd "<http://www.w3.org/2001/XMLSchema#>"})

(def booksgraph (URI. "http://data.deichman.no/books"))
(def reviewsgraph (URI. "http://data.deichman.no/reviews"))
(def sourcegraph (URI. "http://data.deichman.no/sources"))

(defn load-review
  [uri]
  (query
    (from reviewsgraph)
    (from-named booksgraph sourcegraph)
    (select :title :text :teaser :created :issued :modified :audience
            :source :sourcename :reviewer :reviewername :workplace
            :workplacename :edition :editiontitle
            [(group-concat :editionauthor ", ") :editionauthor] :work :worktitle
            [(group-concat :workauthor ", ") :workauthor])
    (where uri [:rev :title] :title \;
               [:dc :abstract] :teaser \;
               [:rev :text] :text \;
               [:dc :created] :created \;
               [:dc :issued] :issued \;
               [:dc :modified] :modified \;
               [:dc :audience] :audience \;
               [:dc :source] :source \;
               [:rev :reviewer] :reviewer \.
           (graph sourcegraph :source [:foaf :name] :sourcename \.)
           (optional
             (graph sourcegraph :reviewer [:foaf :name] :reviewername \;
                                          [:org :memberOf] :workplace \.
                                :workplace [:skos :prefLabel] :workplacename \.))
           (graph booksgraph
             :edition a [:fabio :Manifestation] \;
                      [:rev :hasReview] uri \;
                      [:dc :title] :editiontitle \;
                      [:dc :creator] :edauthor \.
             :edauthor [:foaf :name] :editionauthor \.
             :work a [:fabio :Work] \;
                   [:rev :hasReview] uri \;
                   [:dc :title] :worktitle \;
                   [:dc :creator] :wauthor \.
             :wauthor [:foaf :name] :workauthor))))

(defn save-review
  [uri old updated]
  (query
    (modify reviewsgraph)
    (delete uri [:rev :title] (old :title) \;
                [:dc :abstract] (old :teaser) \;
                [:rev :text] [(old :text) :no] \;
                [:dc :modified] [(old :modified) "xsd:dateTime"] \.)
    (insert uri [:rev :title] (updated :title) \;
                [:dc :abstract] (updated :teaser) \;
                [:rev :text] [(updated :text) :no] \;
                [:dc :modified] [(str (from-time-zone (now) (time-zone-for-offset 1))) "xsd:dateTime"]
                )))

(defn fetch
  [uri]
  (client/get (config :endpoint)
              (merge (config :http-options)
                     {:query-params
                      {"query" (load-review uri)
                       "format" "application/sparql-results+json"}})))
(defn save
  [uri old updated]
  (client/post (config :sparul)
               (merge (config :http-options)
                      {:form-params {:query (save-review uri old updated) }
                       :digest-auth [(config :username) (config :password)]})))

(defn solutions
  [response]
  (for [solution
        (->> response :body parse-string keywordize-keys :results :bindings)]
    (into {}
          (for [[k v] solution]
            [k (:value v)]))))