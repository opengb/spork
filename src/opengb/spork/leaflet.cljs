(ns opengb.spork.leaflet
  (:require
   [ajax.core :as ajax]
   [clojure.spec.alpha :as s]
   [day8.re-frame.http-fx]
   [leaflet]
   [opengb.spork.leaflet-specs :as leaflet-specs]
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [taoensso.timbre :as timbre]))

;; sample map config provider / ring handler for clj side
;; provide uri to this handler in `(register-re-frame uri)` on the cljs side

; (require '[clojure.data.json :as json])
; (require '[ring.util.response :as response])
; (defn tile-config-handler
;   [_req]
;   (-> a-valid-tile-config ;; see specs
;       (json/write-str)
;       (response/response)
;       (response/content-type "application/json")))

;; * re-frame event/sub plumbing

(defn register-re-frame
  [uri]
  (timbre/debug "registering spork/Map at uri" uri)

  (re-frame/reg-event-fx
   ::request-tile-config
   (fn [_ params]
     {:http-xhrio {:method           :post
                   :uri              uri
                   :with-credentials true
                   :headers          {}
                   :params           (or params {})
                   :format           (ajax/json-request-format)
                   :timeout          8000
                   :response-format  (ajax/json-response-format {:keywords? true})
                   :on-success       [::receive-tile-config]
                   :on-failure       [::handle-tile-config-error]}}))

  (re-frame/reg-event-fx
   ::receive-tile-config
   (fn [{:keys [db]} [_ leaflet-config]]
     (let [spec ::leaflet-specs/leaflet-tile-config]
     (if (s/valid? spec leaflet-config)
       {:db (assoc db ::tile-config leaflet-config)}
       {:dispatch [::handle-config-error (s/explain-str spec leaflet-config)]}))))

  (re-frame/reg-event-fx
   ::handle-tile-config-error
   (fn [_ [_ response-data]]
     (timbre/error ::handle-tile-config-error response-data)))

  (re-frame/reg-event-fx
   ::clear-tile-config
   (fn [{:keys [db]} _]
     {:db (dissoc db ::tile-config)}))

  (re-frame/reg-sub
   ::tile-config
   (fn [db _]
     (::tile-config db))))

;; *

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
