(ns opengb.spork.leaflet-specs
  (:require
   [clojure.spec.alpha :as s]))

;; consider moving these to an overall opengb spec lib;
;; stay away until grid is also relying on this proj so we don't clobber ourselves by including
;; this lib as a dep

(s/def ::non-empty-string      (s/and string? #(seq %)))
(s/def ::lat                   (s/double-in :min -90 :max 90 :infinite false :NaN? false))
(s/def ::lng                   (s/double-in :min -180 :max 180 :infinite false :NaN? false))
(s/def ::point                 (s/cat :lng ::lng :lat ::lat))
(s/def ::zoom                  (s/or :double (s/double-in :min 0 :max 20 :infinite false :NaN? false)
                                     :int    (s/int-in 0 20)))

(s/def ::base-tile-attribution ::non-empty-string)
(s/def ::base-tile-subdomains  ::non-empty-string)
(s/def ::base-tile-url         ::non-empty-string)
(s/def ::center                ::point)

(s/def ::leaflet-tile-config
  (s/keys :req-un [::base-tile-subdomains
                   ::base-tile-attribution
                   ::base-tile-url
                   ::center
                   ::zoom]))

(def sample-tile-config
  {:base-tile-attribution "© Example Leaflet Provider (https://example.com/leaflet-provider/copyright/)"
   :base-tile-subdomains "abcd"
   :base-tile-url "https://server.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Base/MapServer/tile/{z}/{y}/{x}"
   :center [45.42517 -75.70007]
   :zoom 16.0})

(comment
 (s/valid? ::base-tile-url "http://foo.bar")
 (s/valid? ::base-tile-url "http://fa://aou//foo.bar")
 (s/valid? ::leaflet-tile-config sample-tile-config)
 (s/explain ::leaflet-tile-config sample-tile-config)
 (s/exercise ::zoom)
 (s/generate ::leaflet-map-config)

 (require '[clojure.test.check.generators :as gen])
 (gen/generate (s/gen ::zoom))
 )
