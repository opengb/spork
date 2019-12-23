(ns opengb.spork.leaflet-cards
  (:require
   [devcards.core :refer [defcard-rg]]
   [opengb.spork :as spork]))

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

(defcard-rg MapWithMarkers
  "Can add markers to a map"
  (fn [*props _]
    [spork/Map @*props])
  {:initial-lat-lng [49.2827 -123.1207]
   :initial-zoom 8
   :marker-attributes {:stroke true :color "red"}
   :markers [{:id 1 :lat-lng [49.2827 -123.1207]}
             {:id 2 :lat-lng [49.2830 -123.1235]}]}
  {:inspect-data true})

(def *map-config (atom nil))

(defcard-rg Re-FrameConfigMap
  (fn [*props _]
    [:<>
     [spork/Map @*props]
     [:pre (str @*map-config)]
     [:button {:on-click #(prn "register")} "Register re-frame bits"]
     [:button {:on-click #(prn "load")} "Load remote config"]
     [:button {:on-click #(prn "clear")} "Clear remote config"]])
  {:map-config *map-config}
  {:inspect-data true})
