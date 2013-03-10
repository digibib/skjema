(ns askjema.views
  (:require [dommy.template :as html])
  (:require-macros [dommy.template-compile :refer [deftemplate]]))

(defn uri-link [uri]
  [:td.uri
   [:a {:href uri} (str \< uri \>)]])

(deftemplate tbody [review edition work audience reviewer worksource]
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
    (uri-link (->> edition first :edition))]
   [:tr#work
    [:td.property "Omtaler verk"]
    [:td.label (str \" (->> work first :worktitle) \" " av " (->> work first :workauthor))]
    (uri-link (->> work first :work))]
   (for [aud audience]
     [:tr.audience
      [:td.property "Målgruppe"]
      [:td.label "-"]
      (uri-link (aud :audience))])
   (for [rev reviewer]
     [:tr.reviewer
      [:td.property "Anmelder"]
      [:td.label (or (rev :reviewername) "(mangler foaf:name)")]
      (uri-link (rev :reviewer))])
   [:tr#workplace
    [:td.property "Arbeidssted"]
    [:td.label (or (->> worksource first :workplacename) "(ikke tilknyttet)")]
    (if (->> worksource first :workplace)
      (uri-link (->> worksource first :workplace))
      [:td.uri "-"])]
   [:tr#source
    [:td.property "Kilde"]
    [:td.label (->> worksource first :sourcename)]
    (uri-link (->> worksource first :source))]])
