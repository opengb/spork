(ns opengb.figwheel-handler
  (:require
   [clojure.data.json :as json]
   [figwheel.server.ring]
   [opengb.spork.leaflet-specs :as leaflet-specs]
   [ring.util.response :as response]))

(def *current-config (atom 0))

(defn tile-config-handler
  "Feeds out a valid map config for testing config loading in devcards."
  [_]
  (let [configs leaflet-specs/sample-tile-configs
        n (rem @*current-config (count configs))
        _ (swap! *current-config inc)
        config (-> configs seq (nth n))]
    (-> config
        (json/write-str)
        (response/response)
        (response/content-type "application/json"))))

(defn handler
  [request]
  (if (= (:uri request) "/tile-config")
    (tile-config-handler request)
    (figwheel.server.ring/not-found request)))
