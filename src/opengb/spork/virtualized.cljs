(ns opengb.spork.virtualized
  (:require
   [cljsjs.react-virtualized]
   [reagent.core :as reagent]))

(defn AutoWidther
  [[Component props]]
  (let [supplied-height (:height props)]
    [:> js/ReactVirtualized.AutoSizer
     {:defaultHeight 200
      :defaultWidth 200
      :disableHeight true}
     (fn [js-args]
       (let [rv-width (.-width js-args)
             props' (-> props
                        (assoc :width rv-width)
                        (assoc :height supplied-height))]
         (reagent/as-element [Component props'])))]))

(defn AutoHeighter
  [[Component props]]
  (let [supplied-width (:width props)]
    [:> js/ReactVirtualized.AutoSizer
     {:defaultwidth 200
      :defaultWidth 200
      :disableWidth true}
     (fn [js-args]
       (let [rv-height (.-height js-args)
             props' (-> props
                        (assoc :height rv-height)
                        (assoc :width supplied-width))]
         (reagent/as-element [Component props'])))]))

(defn AutoSizer
  "Supply a pixel height, let this wrapper expand fixed-size components like
   vega charts or leaflet maps to fill width of containing div. (Args are wrapped
   in a seq so that the callsite resembles hiccup)."
  [[Component props]]
  (let [supplied-height (:height props)]
    [:> js/ReactVirtualized.AutoSizer
     {:defaultHeight 200
      :defaultWidth 200
      :disableHeight true}
     (fn [js-args]
       (let [rv-width (.-width js-args)
             props' (-> props
                        (assoc :width rv-width)
                        (assoc :height supplied-height))]
         (reagent/as-element [Component props'])))]))

(defn AutoSizer2
  "Supply a pixel height, let this wrapper expand fixed-size components like
  vega charts or leaflet maps to fill width of containing div. (Args are wrapped
  in a seq so that the callsite resembles hiccup)."
  [[Component props]]
  [:> js/ReactVirtualized.AutoSizer
   {:defaultHeight 200
    :defaultWidth 200}
   (fn [js-args]
     (let [rv-width (.-width js-args)
           rv-height (.-height js-args)
           props' (-> props
                      (assoc :width rv-width)
                      (assoc :height rv-height))]
       (reagent/as-element [Component props'])))])
