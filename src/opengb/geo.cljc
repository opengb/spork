(ns opengb.geo
  (:require
   [clojure.spec.alpha :as s]))


;; * Specs


(s/def ::non-empty-string (s/and string? #(seq %)))
(s/def ::lat              (s/double-in :min -90 :max 90 :infinite false :NaN? false))
(s/def ::lng              (s/double-in :min -180 :max 180 :infinite false :NaN? false))
(s/def ::zoom             (s/or :double (s/double-in :min 0 :max 20 :infinite false :NaN? false)
                                :int    (s/int-in 0 20)))

;; ** Coordinate Types

; {:lat 49.0 :lng -123.0}
(s/def ::coord   (s/nilable (s/keys :req-un [::lat ::lng])))
(s/def ::non-nil-coord (s/and some? ::coord))

; "(-123.0,49.0)"
(def geom-re #"\((-?\d+(.\d+)?),(-?\d+(.\d+)?)\)")
(s/def ::geom (s/nilable #(re-matches geom-re %)))
(s/def ::non-nil-geom (s/and some? ::geom))

; [49.0 -123.0]
(s/def ::lat-lng (s/nilable (s/cat :lat ::lat :lng ::lng)))
(s/def ::non-nil-lat-lng (s/and some? ::lat-lng))

; [-123.0 49.0]
(s/def ::lng-lat (s/nilable (s/cat :lng ::lng :lat ::lat)))
(s/def ::non-nil-lng-lat (s/and some? ::lng-lat))

(s/def ::has-some-coord (s/or :coord   (s/keys :req-un [::coord])
                              :geom    (s/keys :req-un [::geom])
                              :lat-lng (s/keys :req-un [::lat-lng])
                              :lng-lat (s/keys :req-un [::lng-lat])))

;; ** Bounds
(s/def ::north-east ::coord)
(s/def ::south-west ::coord)
(s/def ::bounds (s/keys :req-un [::north-east ::south-west]))

(s/def ::initial-center ::coord)
(s/def ::initial-bounds ::bounds)


;; * Coordinate Normalization


(defn parse-number
  [s]
  #?(:clj  (Double. s)
     :cljs (js/parseFloat s)))

(defn geom->coord
  [geom]
  {:pre  [(s/valid? ::geom geom)]
   :post [(s/valid? ::coord %)]}
  (if geom
    (let [[_ lng-string _ lat-string _] (re-matches geom-re geom)]
      {:lat (parse-number lat-string)
       :lng (parse-number lng-string)})
    nil))

(defn normalize-coordinates
  "Normalizes Lat-first, Lng-first, and Geom coordinates to a map of lat and lng.
  Invalid input will return coord set to nil."
  [marker]
  {:post [(s/valid? (s/keys :req-un [::coord]) %)]}
  (if (s/valid? ::has-some-coord marker)
    (let [[coord-key conformed-marker] (s/conform ::has-some-coord marker)
          new-coord (case coord-key
                      :geom (geom->coord (get conformed-marker coord-key))
                      (get conformed-marker coord-key))]
      (-> marker
          (dissoc coord-key)
          (assoc :coord new-coord)))
    (-> marker
        (assoc :coord nil))))

(defn nil-coords?
  [{:keys [coord]}]
  (nil? coord))


;; Calculations


(def default-center-and-bounds
  "Should show all of Canada. Don't want the map to blow up without valid inputs,
  and this should hopefully illustrate that there isn't anything on the map."
  {:initial-center  {:lat 55.0 :lng -105.0}
   :initial-bounds  {:north-east {:lat 70.0 :lng -50.0}
                     :south-west {:lat 40.0 :lng -140.0}}})

(defn find-marker-center-and-bounds
  "Finds center point and bounding box of given markers. If no valid markers are
  given, then returns defaults."
  [markers]
  {:post [(s/valid? (s/keys :req-un [::initial-center ::initial-bounds]) %)]}
  (let [coord-markers (->> markers
                           (map normalize-coordinates)
                           (remove nil-coords?))]
    (if (not-empty coord-markers)
      (let [mid           (fn [nums] (+ (/ (- (apply max nums) (apply min nums)) 2) (apply min nums)))
            coords        (map :coord coord-markers)
            lats          (map :lat coords)
            lngs          (map :lng coords)
            north         (apply max lats)
            east          (apply max lngs)
            south         (apply min lats)
            west          (apply min lngs)
            bounds        {:north-east {:lat north :lng east}
                           :south-west {:lat south :lng west}}
            center        {:lat (mid lats) :lng (mid lngs)}]
        {:initial-center center :initial-bounds bounds})
      default-center-and-bounds)))

(defn does-bounds-contain-coord?
  "Takes in a bounds and a coord and returns true if coord is inside bounds"
  [{:keys [north-east south-west] :as bounds}
   {:keys [lat lng] :as coord}]
  {:pre [(s/valid? ::coord coord)
         (s/valid? ::bounds bounds)]}
  (and (< lat (:lat north-east))
       (> lat (:lat south-west))
       (< lng (:lng north-east))
       (> lng (:lng south-west))))


;; * Leaflet Specs and Conversions


(s/def ::leaflet-coord ::lat-lng)
(s/def ::non-nil-leaflet-coord (s/and some? ::leaflet-coord))
(s/def ::leaflet-bounds (s/tuple ::leaflet-coord ::leaflet-coord))

(defn coord->leaflet
  "Converts a coordinate map to [lat lng]"
  [{:keys [lat lng] :as coord}]
  {:pre  [(s/valid? ::coord coord)]
   :post [(s/valid? ::leaflet-coord %)]}
  (if coord
    [lat lng]
    nil))

(defn bounds->leaflet
  "Converts a bounds map to [[ne-lat ne-lng][sw-lat sw-lng]]"
  [{:keys [north-east south-west] :as bounds}]
  {:pre  [(s/valid? ::bounds bounds)]
   :post [(s/valid? ::leaflet-bounds %)]}
  [[(:lat north-east) (:lng north-east)]
   [(:lat south-west) (:lng south-west)]])

;; ** ->JS helpers
#?(:cljs
   (do (defn coord->leaflet-js
         "Converts a coordinate map to a Javascript array of [lat lng]"
         [coord]
         (-> coord
             coord->leaflet
             clj->js))

       (defn bounds->leaflet-js
         "Converts a bounds map to a Javascript array of
         [[ne-lat ne-lng][sw-lat sw-lng]]"
         [bounds]
         (-> bounds
             bounds->leaflet
             clj->js))))
