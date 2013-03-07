(ns askjema.client
  (:require [clojure.browser.repl :as repl]
            [cljs.reader :as reader]
            [goog.net.XhrIo :as xhr]
            [domina :refer [by-id value log value set-value!]]
            [domina.events :as event]))

;(repl/connect "http://localhost:9000/repl")

(defn extract
  [vars solutions]
  (set (remove empty? (map #(select-keys % vars) solutions))))

(defn edn-call
  [path callback method data]
  (xhr/send path
            callback
            method
            (pr-str data)
            (clj->js {"Content-Type" "application/edn"})))

(defn loaded [event]
  (let [response (.-target event)
        solutions (reader/read-string (.getResponseText response))
        review (extract [:title :teaser :text :created :issued :modified] solutions)]
    (do
      (set-value! (by-id "title") (->> review first :title))
      (set-value! (by-id "teaser") (->> review first :teaser))
      (set-value! (by-id "text") (->> review first :text))))
    )

(defn load-review []
  (let [body {:uri (value (by-id "review-uri"))}]
    (edn-call "/review" loaded "POST" body)))

(defn ^:export init []
  (log "Hallo der, mister Åsen.")
  (event/listen! (by-id "load") :click load-review))
