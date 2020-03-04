(ns opengb.spork.leaflet-specs
  (:require
   [clojure.spec.alpha :as s]))

;; consider moving these to an overall opengb spec lib;
;; stay away until grid is also relying on this proj so we don't clobber ourselves by including
;; this lib as a dep

(s/def ::non-empty-string      (s/and string? #(seq %)))
(s/def ::lat                   (s/double-in :min -90 :max 90 :infinite false :NaN? false))
(s/def ::lng                   (s/double-in :min -180 :max 180 :infinite false :NaN? false))

; this should be renamed since MapBox uses `point` to refer to anything with an x,y (could be lnglat, could be pixels.)
(s/def ::point                 (s/cat :lat ::lat :lng ::lng))

(s/def ::zoom                  (s/or :double (s/double-in :min 0 :max 20 :infinite false :NaN? false)
                                     :int    (s/int-in 0 20)))


(s/def ::lat-lng (s/nilable (s/cat :lat ::lat :lng ::lng)))
(s/def ::lng-lat (s/nilable (s/cat :lng ::lng :lat ::lat)))
(s/def ::coord   (s/nilable (s/keys :req-un [::lat ::lng])))

(s/def ::has-some-coords (s/or :lat-lng (s/keys :req-un [::lat-lng])
                               :lng-lat (s/keys :req-un [::lng-lat])
                               :coords  (s/keys :req-un [::coords])))


(s/def ::north-east ::coord)
(s/def ::south-west ::coord)
(s/def ::bounds (s/keys :req-un [::north-east ::south-west]))

(s/def ::leaflet-coord ::lat-lng)
(s/def ::leaflet-bounds (s/tuple ::leaflet-coord ::leaflet-coord))

(s/def ::attribution ::non-empty-string)
(s/def ::subdomains  ::non-empty-string)
(s/def ::url         ::non-empty-string)
(s/def ::min-zoom    ::zoom)
(s/def ::max-zoom    ::zoom)

(s/def ::leaflet-tile-config
  (s/keys :req-un [::attribution
                   ::url]
          :opt-un [::ext
                   ::max-zoom
                   ::min-zoom
                   ::subdomains]))

;; sample configs from https://leaflet-extras.github.io/leaflet-providers/preview/

(def esri-tile-config
  {:url "https://server.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Base/MapServer/tile/{z}/{y}/{x}"
   :subdomains "abcd"
   :attribution "© Example Leaflet Provider (https://example.com/leaflet-provider/copyright/)"})

(def stamen-tile-config
  {:url "https://stamen-tiles-{s}.a.ssl.fastly.net/toner-lite/{z}/{x}/{y}{r}.{ext}"
   :attribution "Map tiles by <a href='http://stamen.com'>Stamen Design</a>, <a href='http://creativecommons.org/licenses/by/3.0'>CC BY 3.0</a> &mdash; Map data &copy; <a href='https://www.openstreetmap.org/copyright'>OpenStreetMap</a> contributors"
   :subdomains "abcd"
   :min-zoom 0
   :max-zoom 20
   :ext "png"})

(def watercolour-tile-config
  {:url "https://stamen-tiles-{s}.a.ssl.fastly.net/watercolor/{z}/{x}/{y}.{ext}"
   :attribution "Map tiles by <a href=\"http://stamen.com\">Stamen Design</a>, <a href=\"http://creativecommons.org/licenses/by/3.0\">CC BY 3.0</a> &mdash; Map data &copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors"
   :subdomains "abcd"
   :minZoom 1
   :maxZoom 16
   :ext "jpg"})

(def sample-tile-configs
  #{esri-tile-config stamen-tile-config watercolour-tile-config})

(comment
 (s/explain ::leaflet-tile-config stamen-tile-config)
 (s/valid? ::base-tile-url "http://foo.bar")
 (s/valid? ::base-tile-url "http://fa://aou//foo.bar")
 (s/valid? ::leaflet-tile-config esri-tile-config)
 (s/explain ::leaflet-tile-config esri-tile-config)
 (s/exercise ::zoom)
 (s/generate ::leaflet-map-config)

 (require '[clojure.test.check.generators :as gen])
 (gen/generate (s/gen ::zoom))
 )
