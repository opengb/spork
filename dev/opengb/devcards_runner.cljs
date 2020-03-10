(ns ^:figwheel-hooks opengb.devcards-runner
  (:require
   [devcards.core :refer [defcard defcard-rg]]
   [opengb.spork.leaflet-cards] ;; require for effect
   [reagent.core :as r]))

(defcard example
  "A simple test card rendered with `defcard`"
  (range 20))

(defcard-rg rg-example
  "A test card with a reagent component rendered with `defcard-rg`"
  (fn [data-atom _]
    (let [color (:color @data-atom)]
    [:div {:style {:color color}}
     "this should be hiccup rendered in " color]))
  (r/atom {:color "blue"}))

(defn ^:after-load mount
  []
  (devcards.core/start-devcard-ui!))

(defn ^:export init
  []
  (stylefy/init)
  (mount))
