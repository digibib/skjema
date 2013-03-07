(ns askjema.client
  (:require [clojure.browser.repl :as repl]
            [cljs.reader :as reader]
            [goog.net.XhrIo :as xhr]
            [dommy.template :as hmtl]
            [dommy.core :as dom])
  (:require-macros [dommy.core-compile :refer [sel sel1]]
                   [dommy.template-compile :refer [node deftemplate]]))

;(repl/connect "http://localhost:9000/repl")

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
  (doseq [e ["title" "teaser" "text"]]
    (set! (.-innerHTML (sel1 (str "#preview-" e)))
          (.-value (sel1 (str "#" e))))))

(deftemplate table-body [review edition work audience reviewer worksource]
  [:tbody
   [:tr#created
    [:td.property "Opprettet"]
    [:td.label (->> review first :created)]
    [:td.uri "-"]]
   [:tr#issued
    [:td.property "Publisert"]
    [:td.label (->> review first :issued)]
    [:td.uri "-"]]
   [:tr#modified
    [:td.property "Sist endret"]
    [:td.label (->> review first :modified)]
    [:td.uri "-"]]
   [:tr#edition
    [:td.property "Omtaler utgave"]
    [:td.label (str \" (->> edition first :editiontitle) \" " av " (->> edition first :editionauthor))]
    [:td.uri
     [:a {:href (->> edition first :edition)} (str \< (->> edition first :edition) \>)]]]
   [:tr#work
    [:td.property "Omtaler verk"]
    [:td.label (str \" (->> work first :worktitle) \" " av " (->> work first :workauthor))]
    [:td.uri
     [:a {:href (->> work first :work)} (str \< (->> work first :work) \>)]]]
   (for [aud audience]
     [:tr.audience
      [:td.property "Målgruppe"]
      [:td.label "-"]
      [:td.uri (str \< (aud :audience) \> )]])
   (for [rev reviewer]
     [:tr.reviewer
      [:td.property "Anmelder"]
      [:td.label (rev :reviewername)]
      [:td.uri
       [:a {:href (rev :reviewer) } (str \< (rev :reviewer) \> )]]])
   [:tr#workplace
    [:td.property "Arbeidssted"]
    [:td.label (->> worksource first :workplacename)]
    [:td.uri
     [:a {:href (->> worksource first :workplace)} (str \< (->> worksource first :workplace) \>)]]]
   [:tr#source
    [:td.property "Kilde"]
    [:td.label (->> worksource first :sourcename)]
    [:td.uri
     [:a {:href (->> worksource first :source)} (str \< (->> worksource first :source) \>)]]]
   ])

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
      (set! (.-value (sel1 "#title")) (->> review first :title))
      (set! (.-data-original-value (sel1 "#title")) (->> review first :title))
      (set! (.-value (sel1 "#teaser")) (->> review first :teaser))
      (set! (.-value (sel1 "#text")) (->> review first :text)))
      (sync-preview)
      (set! (.-innerHTML (sel1 "tbody"))
            (.-innerHTML
              (table-body review edition work audience reviewer worksource)))
    ))

(defn load-review []
  (let [body {:uri (.-value (sel1 "#review-uri"))}]
    (edn-call "/review" loaded "POST" body)))


(defn ^:export init []
  (log "Hallo der, mister Åsen.")
  (dom/listen! (sel1 "#load") :click load-review)
  (dom/listen! (sel1 "#title") :keyup sync-preview)
  (dom/listen! (sel1 "#teaser") :keyup sync-preview)
  (dom/listen! (sel1 "#text") :keyup sync-preview)
  )
