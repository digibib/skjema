(ns askjema.views
  (:require [dommy.template :as html])
  (:require-macros [dommy.template-compile :refer [deftemplate]]))

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
      [:td.label (or (rev :reviewername) "(mangler foaf:name)")]
      [:td.uri
       [:a {:href (rev :reviewer) } (str \< (rev :reviewer) \> )]]])
   [:tr#workplace
    [:td.property "Arbeidssted"]
    [:td.label (or (->> worksource first :workplacename) "(ikke tilknyttet)")]
    (if (->> worksource first :workplace)
      [:td.uri
       [:a {:href (->> worksource first :workplace) } (str \< (->> worksource first :workplace) \>)]]
      [:td.uri "-"])
    ]
   [:tr#source
    [:td.property "Kilde"]
    [:td.label (->> worksource first :sourcename)]
    [:td.uri
     [:a {:href (->> worksource first :source)} (str \< (->> worksource first :source) \>)]]]
   ])
