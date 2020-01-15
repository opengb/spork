(ns opengb.figwheel-handler
  (:require
   [clojure.data.json :as json]
   [figwheel.server.ring]
   [opengb.spork.leaflet-specs :as leaflet-specs]
   [ring.util.response :as response]))

(defn tile-config-handler
  "Feeds out a valid map config for testing config loading in devcards."
  [_]
  (-> leaflet-specs/sample-tile-config
      (json/write-str)
      (response/response)
      (response/content-type "application/json")))

(defn handler
  [request]
  (if (= (:uri request) "/tile-config")
    (tile-config-handler request)
    (figwheel.server.ring/not-found request)))
