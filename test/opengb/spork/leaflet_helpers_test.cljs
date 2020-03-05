(ns opengb.spork.leaflet-helpers-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [opengb.spork.leaflet-helpers :as sut]))

(deftest normalize-coordinates-test
  (testing "Lat-first, Lng-first, and already normalized coordinates should all work."
    (is (= {:coord {:lat 49.5 :lng -123.5}} (sut/normalize-coordinates {:lat-lng [49.5 -123.5]})))
    (is (= {:coord {:lat 49.5 :lng -123.5}} (sut/normalize-coordinates {:lng-lat [-123.5 49.5]})))
    (is (= {:coord {:lat 49.5 :lng -123.5}} (sut/normalize-coordinates {:coord {:lat 49.5 :lng -123.5}}))))
  (testing "Maps without a valid coordinate should return {:coord nil}"
    (is (let [{:keys [coord] :as output} (sut/normalize-coordinates {})]
          (and (contains? output :coord)
               (nil? coord))))
    (is (let [{:keys [coord] :as output} (sut/normalize-coordinates {:lat-lng [-123.5 49.5]})]
          (and (contains? output :coord)
               (nil? coord))))
    (is (let [{:keys [coord] :as output} (sut/normalize-coordinates {:lat-lng "-49.5, -123.5"})]
          (and (contains? output :coord)
               (nil? coord))))))

(deftest find-marker-center-and-bounds-test
  (testing "Valid markers should return bounds."
    (let [{:keys [initial-center initial-bounds]}
          (sut/find-marker-center-and-bounds [{:id 1 :lat-lng [49.2827 -123.1207]}
                                              {:id 2 :lat-lng [49.2830 -123.1235]}])]
      (is (= {:lat 49.282849999999996 :lng -123.1221}
             initial-center))
      (is (= {:north-east {:lat 49.283 :lng -123.1207}
              :south-west {:lat 49.2827 :lng -123.1235}}
             initial-bounds))))

  (testing "Markers with nil lat-lngs should be filtered from the calculations."
    (let [{:keys [initial-center initial-bounds]}
          (sut/find-marker-center-and-bounds [{:id 1 :lat-lng [49.2827 -123.1207]}
                                              {:id 2 :lat-lng [49.2830 -123.1235]}
                                              {:id 3 :lat-lng nil}])]
      (is (= {:lat 49.282849999999996 :lng -123.1221}
             initial-center))
      (is (= {:north-east {:lat 49.283 :lng -123.1207}
              :south-west {:lat 49.2827 :lng -123.1235}}
             initial-bounds))))

  (testing "Input with no valid markers should return default center and bounds"
    (is sut/default-center-and-bounds
        (sut/find-marker-center-and-bounds [{:id 1 :lat-lng [-123.1207 49.2827]}
                                            {:id 2 :lat-lng "49.2830 -123.1235"}
                                            {:id 3 :lat-lng nil}])))

  )

(deftest coord->leaflet
  (testing "Normalized coords should become leaflet coords"
    (is (= [49.283 -123.1207]
           (sut/coord->leaflet {:lat 49.283 :lng -123.1207})))))

(deftest bounds->leaflet
  (testing "Normalized bounds should become leaflet bounds"
    (is (= [[49.283 -123.1207] [49.2827 -123.1235]]
           (sut/bounds->leaflet {:north-east {:lat 49.283 :lng -123.1207}
                                 :south-west {:lat 49.2827 :lng -123.1235}}) ))))
