(ns opengb.spork.leaflet-helpers
  (:require
   [clojure.spec.alpha :as s]
   [opengb.spork.leaflet-specs :as leaflet-specs]))

(defn normalize-coordinates
  [marker]
  {:post [(s/valid? (s/keys :req-un [::coord]) %)]}
  (let [[coord-key conformed-marker] (s/conform ::leaflet-specs/has-some-coords marker)]
    (if (= coord-key :coords)
      marker
      (-> marker
          (dissoc coord-key)
          (assoc :coord (get conformed-marker coord-key))))))

(s/def ::initial-center ::leaflet-specs/coord)
(s/def ::initial-bounds ::leaflet-specs/bounds)

(defn find-marker-bounds
  "Finds bounding box of given markers, and returns map containing center point
   and bounds."
  [markers]
  ; {:post [(s/valid? (s/keys :req-un [::initial-center ::initial-bounds]) %)]}
  (let [coord-markers (map normalize-coordinates markers)
        coords        (map :coord coord-markers)
        valid-coords  (filter some? coords)
        lats          (map :lat valid-coords)
        lngs          (map :lng valid-coords)
        north         (apply max lngs)
        east          (apply max lats)
        south         (apply min lngs)
        west          (apply min lats)
        bounds        {:north-east {:lng north :lat east}
                       :south-west {:lng south :lat west}}
        mid           (fn [nums] (+ (/ (- (apply max nums) (apply min nums)) 2) (apply min nums)))
        center        {:lat (mid lats) :lng (mid lngs)}]
    {:initial-center center :initial-bounds bounds}))

(defn coord->leaflet
  [{:keys [lat lng] :as coord}]
  {:pre  [(s/valid? ::leaflet-specs/coord coord)]
   :post [(s/valid? ::leaflet-specs/leaflet-coord %)]}
  [lat lng])

(defn bounds->leaflet
  [{:keys [north-east south-west] :as bounds}]
  {:pre  [(s/valid? ::leaflet-specs/bounds bounds)]
   :post [(s/valid? ::leaflet-specs/leaflet-bounds %)]}
  [[(:lat north-east) (:lng north-east)]
   [(:lat south-west) (:lng south-west)]])
