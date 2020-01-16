(ns opengb.spork.leaflet
  (:require
   [ajax.core :as ajax]
   [camel-snake-kebab.core :as csk]
   [camel-snake-kebab.extras :as csk.extras]
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
; (require '[clojure.spec.alpha :as s])
; (require '[ring.util.response :as response])
; (defn tile-config-handler
;   [req]
;   (let [config (a-valid-tile-config (:body-params req))]
;     (assert (s/valid? ::leaflet-specs/leaflet-tile-config config))
;     (-> config
;         (json/write-str)
;         (response/response)
;         (response/content-type "application/json"))))

;; * re-frame event/sub plumbing

(defn register-re-frame
  [uri]
  (timbre/debug "registering spork/Map at uri" uri)

  (re-frame/reg-event-fx
   ::request-tile-config
   (fn [_ [event-k params]]
     (timbre/debug event-k)
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
   (fn [{:keys [db]} [event-k leaflet-config]]
     (timbre/debug event-k)
     (let [spec ::leaflet-specs/leaflet-tile-config]
       (if (s/valid? spec leaflet-config)
         {:db (assoc db ::tile-config leaflet-config)}
         {:dispatch [::handle-tile-config-error (s/explain-str spec leaflet-config)]}))))

  (re-frame/reg-event-fx
   ::handle-tile-config-error
   (fn [_ [event-k response-data]]
     (timbre/error event-k response-data)))

  (re-frame/reg-event-fx
   ::clear-tile-config
   (fn [{:keys [db]} _]
     {:db (dissoc db ::tile-config)}))

  (re-frame/reg-sub
   ::tile-config
   (fn [db _]
     (::tile-config db))))

;; * Helpers

(defn- to-camel-case-keywords
  "Camel-cases the keys in `m`. Only supports non-namespaced keywords.
  `:camel-case` -> `:camelCase`.

   Useful for feeding maps to JS constructors, etc."
  [m]
  (csk.extras/transform-keys csk/->camelCaseKeyword m))

(defn make-base-tile-layer
  [tile-config]
  {:pre (s/valid? ::leaflet-specs/leaflet-tile-config tile-config)}
  (let [{url :url} tile-config
        options (-> tile-config
                    (dissoc :url)
                    (to-camel-case-keywords)
                    (clj->js))]
    (.tileLayer ^js/Leaflet leaflet url options)))

(defn got-new-tile-config?
  [old-props new-props]
  (and (not= (:tile-config old-props)
             (:tile-config new-props))
       (s/valid? ::leaflet-specs/leaflet-tile-config
                 (:tile-config new-props))))

(def default-tile-config leaflet-specs/esri-tile-config)

;; * Reagent component

(defn Map
  "Wrap leaflet map"
  [_props]
  (let [*react-ref       (reagent/atom nil)
        *leaflet-map     (atom nil)
        *base-tile-layer (atom nil)
        *marker-layers   (atom [])]
    (reagent/create-class
     {:component-did-mount
      (fn map-did-mount [c]
        (let [{:keys [initial-lat-lng
                      initial-zoom
                      min-zoom
                      max-zoom
                      show-draw-control
                      show-zoom-control
                      z-index
                      use-default-tiles?
                      tile-config]
               :or   {initial-lat-lng    [0 0]
                      initial-zoom       10
                      min-zoom           0
                      max-zoom           17
                      show-draw-control  false
                      show-zoom-control  true
                      use-default-tiles? false
                      z-index            1}
               :as _props} (reagent/props c)
              min-zoom (min min-zoom initial-zoom)
              max-zoom (max max-zoom initial-zoom)]
          (reset! *leaflet-map
                  ^js/Leaflet.Map (.map ^js/Leaflet leaflet
                                        @*react-ref
                                        (clj->js {:drawControl     show-draw-control
                                                  :zoomControl     show-zoom-control
                                                  :scrollWheelZoom false
                                                  :zIndex          z-index
                                                  :minZoom         min-zoom
                                                  :maxZoom         max-zoom})))

          (assert ::leaflet-specs/point initial-lat-lng)
          (assert ::leaflet-specs/zoom  initial-zoom)
          (.setView @*leaflet-map (clj->js initial-lat-lng) initial-zoom)

          ;; set up tile layer
          (cond
           use-default-tiles?
           (do (reset! *base-tile-layer (make-base-tile-layer default-tile-config))
               (.addTo @*base-tile-layer @*leaflet-map))

           (s/valid? ::leaflet-specs/leaflet-tile-config tile-config)
           (do (reset! *base-tile-layer (make-base-tile-layer tile-config))
               (.addTo @*base-tile-layer @*leaflet-map)))))

      :component-did-update
      (fn map-did-update [c [_ & old-argv]]
        (let [old-props (first old-argv)
              {:keys [markers on-marker-click
                      use-default-tiles? tile-config] :as new-props} (reagent/props c)
              leaflet-map ^js/Leaflet.Map @*leaflet-map
              did-resize? true
              current-zoom  (.getZoom leaflet-map)
              current-view (-> (.getBounds leaflet-map) (.getCenter))]

          ;; swap tile layer (might have been remote)
          (when (and (not use-default-tiles?)
                     (got-new-tile-config? old-props new-props))
            (when @*base-tile-layer (.remove @*base-tile-layer))
            (reset! *base-tile-layer (make-base-tile-layer tile-config))
            (.addTo @*base-tile-layer @*leaflet-map))

          ;; jic outer DOM el bounds shifted, force-reload edge tiles
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
                       :or   {width 480 height 400} :as _props}]
        [:div {:style {:height height :width width}
               :ref #(reset! *react-ref %)}])})))
