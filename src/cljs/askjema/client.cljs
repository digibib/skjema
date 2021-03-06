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
            old (or (.-data-original-value elem) "")
            nju (or (.-value elem) "")]
        (if (not= old nju)
          (dom/add-class! elem "altered")
          (dom/remove-class! elem "altered"))
        (set! (.-innerHTML (sel1 (str "#preview-" e)))
              (.-value (sel1 (str "#" e))))))
    (if (zero? (.-length (sel ".altered")))
      (set! (.-disabled (sel1 "#save")) true)
      (set! (.-disabled (sel1 "#save")) false))))

(defn saved-sync []
  (do
    (doseq [e ["title" "teaser" "text"]]
      (let [elem (sel1 (str "#" e))
            old (.-data-original-value (or elem ""))
            nju (.-value (or elem ""))]
        (dom/remove-class! elem "altered")
        (set! (.-data-original-value elem)
              (.-value (sel1 (str "#" e))))))
    (set! (.-disabled (sel1 "#save")) false)))

(defn reset-page! []
  (do
    (.reset (sel1 "#review-form"))
    (sync-preview)
    (set! (.-innerHTML (sel1 "tbody")) "")))

(defn feedback [msg]
  (set! (.-innerHTML (sel1 "#message")) msg))

(defn wait-please! []
  (feedback "<img src='skjema/img/loading.gif'>"))

(defn loaded [event]
  (let [response (.-target event)
        status (.getStatus response)]
    (if (= status 200)
      (let [solutions (reader/read-string (.getResponseText response))
            review (extract [:title :teaser :text :created :issued :modified] solutions)
            edition (extract [:edition :editiontitle :editionauthor] solutions)
            work (extract [:work :worktitle :workauthor] solutions)
            audience (extract [:audience] solutions)
            reviewer (extract [:reviewer :reviewername] solutions)
            worksource (extract [:workplace :workplacename :source :sourcename] solutions)]
        (do
          (set! (.-value (sel1 "#title")) (->> review first :title))
          (set! (.-data-original-value (sel1 "#title")) (->> review first :title))
          (set! (.-value (sel1 "#teaser")) (or (->> review first :teaser) ""))
          (set! (.-data-original-value (sel1 "#teaser")) (->> review first :teaser))
          (set! (.-value (sel1 "#text")) (->> review first :text)))
        (set! (.-data-original-value (sel1 "#text")) (->> review first :text))
        (set! (.-data-original-value (sel1 "#review-uri"))
              (.trim (.-value (sel1 "#review-uri"))))
        (sync-preview)
        (set! (.-innerHTML (sel1 "tbody"))
              (.-innerHTML
                (views/tbody review edition work audience reviewer worksource)))
        (feedback "(200) OK. Anbefaling åpnet."))
      (let [msg (reader/read-string (.getResponseText response))]
        (feedback (str "(" status ") " msg))
        (reset-page!)))))

(defn saved [event]
  (let [response (.-target event)
        status (.getStatus response)
        msg (reader/read-string (.getResponseText response))]
    (feedback (str "(" status ") " msg))
    (when (= status 200)
      (saved-sync))))

(defn load-review []
  (let [body {:uri (.trim (.-value (sel1 "#review-uri")))}]
    (wait-please!)
    (edn-call "skjema/load" loaded "POST" body)))

(defn save-review []
  (let [uri (.-data-original-value (sel1 "#review-uri"))
        old {:title (.-data-original-value (sel1 "#title"))
             :teaser (.-data-original-value (sel1 "#teaser"))
             :text (.-data-original-value (sel1 "#text"))}
        updated {:title (.-value (sel1 "#title"))
                 :teaser (.-value (sel1 "#teaser"))
                 :text (.-value (sel1 "#text"))}
        body {:uri uri :old old :updated updated}]
    (edn-call "skjema/save" saved "PUT" body)
    (wait-please!)))

(defn ^:export init []
  (dom/listen! (sel1 "#load") :click load-review)
  (dom/listen! (sel1 "#save") :click save-review)
  (dom/listen! (sel1 "#title") :keyup sync-preview)
  (dom/listen! (sel1 "#teaser") :keyup sync-preview)
  (dom/listen! (sel1 "#text") :keyup sync-preview))