(ns opengb.spork.leaflet
  (:require
   [leaflet]
   [reagent.core :as reagent]
   [taoensso.timbre :as timbre]))

(def map-styles
  {:toner-lite
   {:url "https://stamen-tiles-{s}.a.ssl.fastly.net/toner-lite/{z}/{x}/{y}{r}.{ext}"
    :subdomains "abcd"
    :attribution "Map tiles by <a href='http://stamen.com'>Stamen Design</a>, <a href='http://creativecommons.org/licenses/by/3.0'>CC BY 3.0</a> &mdash; Map data &copy; <a href='https://www.openstreetmap.org/copyright'>OpenStreetMap</a> contributors"
    :minZoom 0
    :maxZoom 20
    :ext "png"}

   :esri-world-gray-canvas
   {:url "https://server.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Base/MapServer/tile/{z}/{y}/{x}"
    ; :url "https://server.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Dark_Gray_Base/MapServer/tile/{z}/{y}/{x}"
    :attribution "Tiles &copy; Esri &mdash; Esri, DeLorme, NAVTEQ"
    :maxZoom 16}})

(defn make-basemap-layer
  [{:keys [style-key max-zoom]
    :or {style-key #_:toner-lite :esri-world-gray-canvas}}]
  (let [style (map-styles style-key)]
    (.tileLayer
     ^js/Leaflet leaflet
     (:url style)
     (clj->js (merge style {:maxZoom max-zoom})))))

(defn Map
  "Wrap leaflet map"
  [props]
  (let [*react-ref (reagent/atom nil)
        *leaflet-map (atom nil)
        *marker-layers (atom [])]
    (reagent/create-class
     {:component-did-mount
      (fn map-did-mount [c]
        (let [{:keys [initial-lat-lng
                      initial-max-bounds
                      initial-zoom
                      max-zoom
                      min-zoom
                      show-draw-control
                      show-zoom-control
                      z-index]
               :or {initial-lat-lng [0 0]
                    initial-max-bounds [[49 -65] [24 -127]]
                    initial-zoom 10
                    max-zoom 17
                    min-zoom 0
                    show-draw-control false
                    show-zoom-control true
                    z-index 1}} (reagent/props c)
              min-zoom (min min-zoom initial-zoom)
              max-zoom (max max-zoom initial-zoom)
              map-node
              ^js/Leaflet.Map
              (.map ^js/Leaflet leaflet
                    @*react-ref
                    (clj->js {:drawControl show-draw-control
                              :zoomControl show-zoom-control
                              :scrollWheelZoom false
                              :zIndex z-index
                              ; :maxBounds initial-max-bounds
                              :minZoom min-zoom
                              :maxZoom max-zoom}))
              map-layer (make-basemap-layer {:z-index z-index :max-zoom max-zoom})]
          (.setView map-node (clj->js initial-lat-lng) initial-zoom)
          (.addTo map-layer map-node)
          (reset! *leaflet-map map-node)))

      :component-did-update
      (fn map-did-update [c]
        (let [{:keys [markers on-marker-click]} (reagent/props c)
              leaflet-map ^js/Leaflet.Map @*leaflet-map
              did-resize? true
              current-zoom  (.getZoom leaflet-map)
              current-view (-> (.getBounds leaflet-map)
                               (.getCenter))]
          (.invalidateSize leaflet-map did-resize?)
          (.setView leaflet-map
                    current-view
                    current-zoom
                    ;; prevent pan animations on a resize
                    #js {:reset did-resize?})

          ;; blow away old markers
          (->> @*marker-layers
               (map (fn destroy-marker
                      [^js/Leaflet.CircleMarker marker-obj]
                      (.removeLayer leaflet-map marker-obj))
                    @*marker-layers)
               (doall))
          (reset! *marker-layers [])

          ;; create all new markers from data
          (->> markers
               (map (fn create-marker
                      [{:keys [id lat-lng marker-attributes tooltip]
                        :or {marker-attributes {:stroke true :color "magenta"}}}]
                      (let [marker-obj
                            ^js/Leaflet.CircleMarker
                            (.circleMarker ^js/Leaflet leaflet
                                           (clj->js lat-lng)
                                           (clj->js marker-attributes))]
                        (.bindTooltip marker-obj tooltip #js {:direction "top"})
                        (.on marker-obj "click" #(on-marker-click id))
                        (swap! *marker-layers conj marker-obj)
                        (.addTo marker-obj leaflet-map))))
               (doall))))

      :reagent-render
      (fn map-render [{:keys [width height]
                       :or {width 480 height 400}}]
        [:div {:style {:height height
                       :width width}
               :ref #(reset! *react-ref %)} "Loading map"])})))
