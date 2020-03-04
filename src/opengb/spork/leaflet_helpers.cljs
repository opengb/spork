(ns opengb.spork.leaflet-helpers
  (:require
   [clojure.spec.alpha :as s]
   [opengb.spork.leaflet-specs :as leaflet-specs]))

(s/def ::initial-lat-lng ::leaflet-specs/lat-lng)
(s/def ::initial-bounds  ::leaflet-specs/bounds)

(defn find-marker-bounds
  "Finds bounding box of given markers, and returns map containing center point
   and bounds."
  [markers]
  {:post [(s/valid? (s/keys :req-un [::initial-lat-lng ::initial-bounds]) %)]}
  (let [lat-lngs (map :lat-lng markers)
        valid-lat-lngs (filter some? lat-lngs)
        lats     (map first valid-lat-lngs)
        lngs     (map second valid-lat-lngs)
        north    (apply max lngs)
        east     (apply max lats)
        south    (apply min lngs)
        west     (apply min lats)
        bounds   [[east north] [west south]]
        mid      (fn [nums] (+ (/ (- (apply max nums) (apply min nums)) 2) (apply min nums)))
        center   [(mid lats) (mid lngs)]]
    {:initial-lat-lng center :initial-bounds bounds}))

(comment


 (find-marker-bounds [{:id 1 :lat-lng [49.2827 -123.1207]}
                      {:id 2 :lat-lng [49.2830 -123.1235]}])

 (find-marker-bounds [{:id 1 :lat-lng [49.2827 -123.1207]}
                      {:id 2 :lat-lng [49.2830 -123.1235]}
                      {:id 3 :lat-lng nil}]))

