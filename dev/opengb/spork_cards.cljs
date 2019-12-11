(ns opengb.spork.cards
  (:require
   [devcards.core :refer [defcard defcard-rg]]
   [opengb.spork :as spork]
   [reagent.core :as r]))

(defcard-rg Map
  "A bare, no-arg map."
  (fn [*props _]
    [spork/Map @*props])
  {}
  {:inspect-data true})

(defcard-rg Map2
  "Setting the initial centre, etc."
  (fn [*props _]
    [spork/Map @*props])
  {:initial-lat-lng [49.2827 -123.1207]
   :initial-zoom 8
   :width 300 :height 300}
  {:inspect-data true})
