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
  {:use-default-tiles? true}
  {:inspect-data true})

(defcard-rg Map2
  "Setting the initial centre, etc."
  (fn [*props _]
    [spork/Map @*props])
  {:initial-lat-lng [49.2827 -123.1207]
   :initial-zoom 8
   :width 300 :height 300
   :use-default-tiles? true}
  {:inspect-data true})

(defcard-rg MapWithMarkers
  "Can add markers with default colouring to a map"
  (fn [*props _]
    [spork/Map @*props])
  {:initial-lat-lng [49.2827 -123.1207]
   :initial-zoom 16
   :markers [{:id 1 :lat-lng [49.2827 -123.1207]}
             {:id 2 :lat-lng [49.2830 -123.1235]}]
   :use-default-tiles? true}
  {:inspect-data true})

(defcard-rg AutoFittingToMarkers
  "A map can auto-fit to its supplied markers *on mount only* with `fit-to-map?`"
  (fn [*props _]
    [spork/Map @*props])
  {:fit-to-markers? true
   :markers [{:id 1 :lat-lng [49.2827 -123.1207] :tooltip "Vancouver"
              :marker-attributes {:stroke true :color "red"}}
             {:id 2 :lat-lng [19.4326 -99.1332] :tooltip "Mexico City"
              :marker-attributes {:stroke true :color "blue"}}]
   :on-marker-click #(js/alert (str "marker click " %))
   :use-default-tiles? true}
  {:inspect-data true})

(defn SubscribingSporkMap
  []
  (let [*config (subscribe [::leaflet/tile-config])]
    (fn []
      [:<>
       [spork/Map {:initial-zoom 9
                   :initial-lat-lng [49.2827 -123.1207]
                   :use-default-tiles? false
                   :tile-config @*config}]
       [:hr]
       [:div "received map configuration:" [:pre (with-out-str (pp/pprint @*config))]]])))

(defonce *config-state
  (reagent/atom {:registered? false}))

(defcard-rg ReFrameConfigMap
  "A map demonstrating how to initialize the re-frame Leaflet events, and fetch and use
   a remote map configuration."
  (fn [_]
    (let [button-style {:flex "0 0 30%"
                        :padding "1rem"
                        :margin-right "1rem"
                        :font-size "1em"}]
      [:<>
       [:div {:style {:margin-bottom "1rem"
                      :display "flex"}}
        [:button {:on-click #(do (leaflet/register-re-frame)
                                 (swap! *config-state assoc :registered? true))
                  :style button-style}
         "Register config handlers"]
        [:button {:on-click #(dispatch [::leaflet/request-tile-config "/tile-config"])
                  :style button-style
                  :disabled (not (:registered? @*config-state))}
         "Request config"]
        [:button {:on-click #(dispatch [::leaflet/clear-tile-config])
                  :style button-style
                  :disabled (not (:registered? @*config-state))}
         "Clear config"]]
       (if (:registered? @*config-state)
         [SubscribingSporkMap]
         [:div {:style {:border "1px solid black"
                        :box-sizing "border-box"
                        :padding "3rem"
                        :width 480
                        :height 400}}
          "Can't show map yet; re-frame not registered"])])))
