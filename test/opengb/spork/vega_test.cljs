(ns opengb.spork.vega-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [opengb.spork.vega :as sut]))

(deftest make-histogram-model-test
  (let [values [679
                1728.1
                232.6
                241.5
                773.9
                246.7
                153.9
                226.8
                336.2
                372.7
                202.4]
        model  (sut/make-histogram-model values)]
    (testing "Bin extent should use the min and max values."
      (is (= [153.9 1728.1] (get-in model [:signals :bin-extent]))))
    (testing "Bin step should be within the range of values."
      (is (<= (get-in model [:signals :bin-step])
              (- 1728.1 153.9)))))
  (testing "Should gracefully handle the empty sequence."
    (let [model (sut/make-histogram-model '())]
      (is (= [0 0] (get-in model [:signals :bin-extent])))
      (is (= 0 (get-in model [:signals :bin-step]))))))
