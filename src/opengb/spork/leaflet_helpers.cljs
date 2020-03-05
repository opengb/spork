(ns opengb.spork.leaflet-helpers
  (:require
   [clojure.spec.alpha :as s]
   [opengb.spork.leaflet-specs :as leaflet-specs]))

(defn normalize-coordinates
  "Normalizes Lat-first and Lng-first coordinates to a map of lat and lng.
  Invalid input will return coord set to nil."
  [marker]
  {:post [(s/valid? (s/keys :req-un [::coord]) %)]}
  (if (s/valid? ::leaflet-specs/has-some-coord marker)
    (let [[coord-key conformed-marker] (s/conform ::leaflet-specs/has-some-coord marker)]
      (if (= coord-key :coord)
        marker
        (-> marker
            (dissoc coord-key)
            (assoc :coord (get conformed-marker coord-key)))))
    (-> marker
        (assoc :coord nil))))

(s/def ::initial-center ::leaflet-specs/coord)
(s/def ::initial-bounds ::leaflet-specs/bounds)

(defn find-marker-center-and-bounds
  "Returns map containing the center point and bounds of given markers."
  [markers]
  {:post [(s/valid? (s/keys :req-un [::initial-center ::initial-bounds]) %)]}
  (let [mid           (fn [nums] (+ (/ (- (apply max nums) (apply min nums)) 2) (apply min nums)))
        coord-markers (->> markers
                           (map normalize-coordinates)
                           (filter #(s/valid? ::leaflet-specs/non-nil-coord (:coord %))))
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
    {:initial-center center :initial-bounds bounds}))

(defn coord->leaflet
  "Converts a coordinate map to [lat lng]"
  [{:keys [lat lng] :as coord}]
  {:pre  [(s/valid? ::leaflet-specs/coord coord)]
   :post [(s/valid? ::leaflet-specs/leaflet-coord %)]}
  [lat lng])

(defn bounds->leaflet
  "Converts a bounds map to [[ne-lat ne-lng][sw-lat sw-lng]]"
  [{:keys [north-east south-west] :as bounds}]
  {:pre  [(s/valid? ::leaflet-specs/bounds bounds)]
   :post [(s/valid? ::leaflet-specs/leaflet-bounds %)]}
  [[(:lat north-east) (:lng north-east)]
   [(:lat south-west) (:lng south-west)]])
