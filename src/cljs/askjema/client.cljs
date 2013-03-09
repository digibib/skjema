(ns askjema.client
  (:require [clojure.browser.repl :as repl]
            [cljs.reader :as reader]
            [goog.net.XhrIo :as xhr]
            [dommy.template :as html]
            [dommy.core :as dom]
            [askjema.views :as views])
  (:require-macros [dommy.core-compile :refer [sel sel1]]))

(defn log [& more]
  (.log js/console (apply str more)))

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

(defn sync-preview []
  (do
    (doseq [e ["title" "teaser" "text"]]
      (let [elem (sel1 (str "#" e))
            old (.-data-original-value elem)
            nju (.-value elem)]
        (if (not= old nju)
          (dom/add-class! elem "altered")
          (dom/remove-class! elem "altered"))
        (set! (.-innerHTML (sel1 (str "#preview-" e)))
              (.-value (sel1 (str "#" e))))))
    (if (= 0 (.-length (sel ".altered")))
      (set! (.-disabled (sel1 "#save")) true)
      (set! (.-disabled (sel1 "#save")) false))))

(defn feedback [msg]
  (set! (.-innerHTML (sel1 "#message")) msg))

(defn wait-please! []
  (feedback "<img src='img/loading.gif'>"))

(defn loaded [event]
  (let [response (.-target event)
        solutions (reader/read-string (.getResponseText response))
        review (extract [:title :teaser :text :created :issued :modified] solutions)
        edition (extract [:edition :editiontitle :editionauthor] solutions)
        work (extract [:work :worktitle :workauthor] solutions)
        audience (extract [:audience] solutions)
        reviewer (extract [:reviewer :reviewername] solutions)
        worksource (extract [:workplace :workplacename :source :sourcename] solutions)]
    (do
      (set! (.-value (sel1 "#modified")) (->> review first :modified))
      (set! (.-value (sel1 "#title")) (->> review first :title))
      (set! (.-data-original-value (sel1 "#title")) (->> review first :title))
      (set! (.-value (sel1 "#teaser")) (->> review first :teaser))
      (set! (.-data-original-value (sel1 "#teaser")) (->> review first :teaser))
      (set! (.-value (sel1 "#text")) (->> review first :text)))
      (set! (.-data-original-value (sel1 "#text")) (->> review first :text))
      (sync-preview)
      (set! (.-innerHTML (sel1 "tbody"))
            (.-innerHTML
              (views/tbody review edition work audience reviewer worksource)))
      (feedback "OK! Anbefaling åpnet.")))

(defn load-review []
  (let [body {:uri (.trim (.-value (sel1 "#review-uri")))}]
    (wait-please!)
    (edn-call "/load" loaded "POST" body)))

(defn save-review []
  (let [uri (.-value (sel1 "#review-uri"))
        old {:title (.-data-original-value (sel1 "#title"))
             :teaser (.-data-original-value (sel1 "#teaser"))
             :text (.-data-original-value (sel1 "#text"))
             :modified (.-value (sel1 "#modified"))}
        updated {:title (.-value (sel1 "#title"))
                 :teaser (.-value (sel1 "#teaser"))
                 :text (.-value (sel1 "#text"))}
        body {:uri uri :old old :updated updated}]
    (edn-call "/save" load-review "PUT" body)
    (wait-please!)))

(defn ^:export init []
  (log "Hallo der, mister Åsen.")
  (dom/listen! (sel1 "#load") :click load-review)
  (dom/listen! (sel1 "#save") :click save-review)
  (dom/listen! (sel1 "#title") :keyup sync-preview)
  (dom/listen! (sel1 "#teaser") :keyup sync-preview)
  (dom/listen! (sel1 "#text") :keyup sync-preview))

;; Debug

;(repl/connect "http://localhost:9000/repl")