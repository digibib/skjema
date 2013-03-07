(ns askjema.sparql
  (:refer-clojure :exclude [filter concat group-by max min count])
  (:require [clj-http.client :as client]
            [boutros.matsu.sparql :refer :all]
            [boutros.matsu.core :refer [register-namespaces]]
            [cheshire.core :refer [parse-string]]
            [clojure.walk :refer [keywordize-keys]])
  (:import java.net.URI))

(def endpoint "http://marc2rdf.deichman.no:8890/sparql")

(register-namespaces {:skos "<http://www.w3.org/2004/02/skos/core#>"
                      :deich "<http://data.deichman.no/>"
                      :foaf "<http://xmlns.com/foaf/0.1/>"
                      :dc "<http://purl.org/dc/terms/>"
                      :rdfs "<http://www.w3.org/2000/01/rdf-schema#>"
                      :fabio "<http://purl.org/spar/fabio/>"
                      :rev "<http://purl.org/stuff/rev#>"
                      :org "<http://www.w3.org/ns/org#>"})

(def booksgraph (URI. "http://data.deichman.no/books"))
(def reviewsgraph (URI. "http://data.deichman.no/reviews"))
(def sourcegraph (URI. "http://data.deichman.no/sources"))

(defn review
  [uri]
  (query
    (from reviewsgraph)
    (from-named booksgraph sourcegraph)
    (select :title :text :teaser :created :issued :modified :audience
            :source :sourcename :reviewer :reviewername :workplace
            :workplacename :edition :editiontitle :editionauthor
            :work :worktitle :workauthor)
    (where uri [:rev :title] :title \;
               [:dc :abstract] :teaser \;
               [:rev :text] :text \;
               [:dc :created] :created \;
               [:dc :issued] :issued \;
               [:dc :modified] :modified \;
               [:dc :audience] :audience \;
               [:dc :source] :source \;
               [:rev :reviewer] :reviewer \.
           (graph sourcegraph)
           (group
             :reviewer [:foaf :name] :reviewername \;
                       [:org :memberOf] :workplace \.
             :workplace [:skos :prefLabel] :workplacename \.
             :source [:foaf :name] :sourcename \.)
           (graph booksgraph)
           (group
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

(defn fetch
  [uri]
  (client/get endpoint
              {:query-params {"query" (review uri)
                              "format" "application/sparql-results+json"}}))

(defn bindings
  [response]
  (let [{{vars :vars} :head {solutions :bindings} :results}
        (->> response :body parse-string keywordize-keys)
        vars (map keyword vars)]
    (into {}
          (for [v vars]
            [v (set (keep #(->> % v :value) solutions))]))))

(defn solutions
  [response]
  (for [solution
        (->> response :body parse-string keywordize-keys :results :bindings)]
    (into {}
          (for [[k v] solution]
            [k (:value v)]))))

(defn extract
  [vars solutions]
  (set (remove empty? (map #(select-keys % vars) solutions))))