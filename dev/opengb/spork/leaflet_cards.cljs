(ns opengb.spork.leaflet-cards
  (:require
   [clojure.pprint :as pp]
   [devcards.core :refer [defcard-rg]]
   [reagent.core :as reagent]
   [re-frame.core :as re-frame :refer [dispatch subscribe]]
   [opengb.spork.leaflet :as leaflet]
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

(defn SubscribingSporkMap
  []
  (let [*config (subscribe [::leaflet/tile-config])]
    (fn []
      [:<>
       [spork/Map {:tile-config @*config}]
       [:hr]
       [:div "received map configuration:" [:pre (with-out-str (pp/pprint @*config))]]])))

(defonce *config-state
  (reagent/atom {:registered? false}))

(defcard-rg ReFrameConfigMap
  "A map demonstrating how to initialize the re-frame Leaflet events, and fetch and use
   a remote map configuration."
  (fn [_]
    [:<>
     (if (:registered? @*config-state)
       [SubscribingSporkMap]
       [:div {:style {:border "1px solid black"
                      :box-sizing "border-box"
                      :padding "3rem"
                      :width 480
                      :height 400}}
        "Can't show map yet; re-frame not registered"])
     [:button {:on-click #(do (leaflet/register-re-frame "/tile-config")
                              (swap! *config-state assoc :registered? true))}
      "Register config handlers with re-frame"]
     [:button {:on-click #(dispatch [::leaflet/request-tile-config])
               :disabled (not (:registered? @*config-state))}
      "Request config"]
     [:button {:on-click #(dispatch [::leaflet/clear-tile-config])
               :disabled (not (:registered? @*config-state))}
      "Clear config"]]))
