(ns opengb.spork.vega
  (:require
   [cljsjs.vega]
   [reagent.core :as reagent]))

(defn update-component
  [*vega-view this-component]
  (let [props (reagent/props this-component)
        {:keys [width height data signals debug]} props]
    ; (js/console.log "vega update" width height data)
    ;; update size
    (.signal @*vega-view "height" height)
    (.signal @*vega-view "width" width)

    ;; completely replace datasets
    (doall
     (for [[dataset-name ms] data]
       (.change @*vega-view (name dataset-name)
                (-> js/vega ;; advanced compilation??
                    (.changeset)
                    (.remove (constantly true))
                    (.insert (clj->js ms))))))
    ;; update signals
    (doall
     (for [[signal-name x] signals]
       (.signal @*vega-view (name signal-name)
                (clj->js x))))
    (.run @*vega-view)
    (doseq [dataset-name debug]
      (js/console.log dataset-name (.data @*vega-view dataset-name)))))

(defn mount-component
  [*vega-view *a-ref this-component]
  (let [props (reagent/props this-component)
        vega-spec (or (:vega-spec props) {})
        on-click (:on-click props)
        runtime (.parse js/vega vega-spec)
        view (-> (js/vega.View. runtime)
                 (.initialize @*a-ref)
                 (.addEventListener "click" #(on-click %))
                 (.renderer "svg")
                 (.hover))]
    ;; uncomment to debug in signal expressions
    ;; see https://vega.github.io/vega/docs/expressions/#debug
    ;; (.logLevel view js/vega.Warn)
    (.run view)
    ; (js/console.log "vega mount" width height)
    (reset! *vega-view view)
    ;; give it one initial bump, in case of static data
    (update-component *vega-view this-component)))

(defn Chart
  "Renderer for a generic Vega chart"
  [_props]
  (let [*a-ref (atom nil)
        *vega-view (atom nil)]
    (reagent/create-class
     {:component-did-mount (partial mount-component *vega-view *a-ref)
      :component-did-update (partial update-component *vega-view)
      :reagent-render
      (fn [{:keys [width height]}]
        [:div {:ref #(reset! *a-ref %)
               :width width
               :height height}])})))

;; * Histogram calculation

(defn freedman-diaconis
  "Figures out binning from data, based on Freedman-Diaconis rule
  See https://stats.stackexchange.com/questions/798/calculating-optimal-number-of-bins-in-a-histogram"
  [{:keys [min max count]}]
  (if (> count 0)
    (/ (- max min) (js/Math.sqrt count))
    0))

(defn get-extents
  "Provides the min and max of the given sequence `s`.
  Evaluates to [0 0] when `s` is empty."
  [s]
  (let [sorted (sort s)
        min    (or (first sorted) 0)
        max    (or (last sorted) 0)]
    [min max]))

(defn make-histogram-model
  "Turns a seq of numeric `values` into a map with `:data` and `:signals` suitable for a Vega histogram.
  Accepts an optional `bin-step-fn` to customize how values are grouped into bins.
  `bin-step-fn` must accept a map with `:min`, `:max`, and `:count`, evaluating to a float representing the bin size."
  ([values]
   (make-histogram-model
    freedman-diaconis
    values))
  ([bin-step-fn values]
   (let [sorted    (sort values)
         [min max] (get-extents sorted)
         bin-step  (bin-step-fn {:min min :max max :count (count values)})]
     {:signals {:bin-step   bin-step
                :bin-extent [min max]}
      :data    {:points (map #(hash-map :u %) sorted)}})))
