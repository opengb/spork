(ns opengb.figwheel-handler
  (:require
   [clojure.data.json :as json]
   [figwheel.server.ring]
   [ring.util.response :as response]))

(defn map-config-handler
  "Feeds out a valid map config for testing config loading in devcards."
  [_]
  (-> {:foo "baz"}
      (json/write-str)
      (response/response)
      (response/content-type "application/json")))

(defn handler
  [request]
  (if (= (:uri request) "/tile-config")
    (map-config-handler request)
    (figwheel.server.ring/not-found request)))
