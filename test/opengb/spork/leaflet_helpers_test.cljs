(ns opengb.spork.leaflet-helpers-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [opengb.spork.leaflet-helpers :as sut]))

(deftest find-marker-bounds-test
  (testing "Valid markers should return bounds."
    (let [{:keys [initial-lat-lng initial-bounds]}
          (sut/find-marker-bounds [{:id 1 :lat-lng [49.2827 -123.1207]}
                                   {:id 2 :lat-lng [49.2830 -123.1235]}])]
      (is (= initial-lat-lng [49.282849999999996 -123.1221]))
      (is (= initial-bounds  [[49.283 -123.1207] [49.2827 -123.1235]]))))
  (testing "Markers with nil lat-lngs should be filtered from the calculations."
    (let [{:keys [initial-lat-lng initial-bounds]}
          (sut/find-marker-bounds [{:id 1 :lat-lng [49.2827 -123.1207]}
                                   {:id 2 :lat-lng [49.2830 -123.1235]}
                                   {:id 3 :lat-lng nil}])]
      (is (= initial-lat-lng [49.282849999999996 -123.1221]))
      (is (= initial-bounds  [[49.283 -123.1207] [49.2827 -123.1235]])))))
