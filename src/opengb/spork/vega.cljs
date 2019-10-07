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
  [props]
  (let [*a-ref (atom nil)
        *vega-view (atom nil)]
    (reagent/create-class
     {:component-did-mount (partial mount-component *vega-view *a-ref)
      :component-did-update (partial update-component *vega-view)
      :reagent-render
      (fn [{:keys [width height]}]
        [:div {:ref #(reset! *a-ref %)
               #_#_:style {:outline "1px dotted red"
                           :position "relative !important"}
               :width width
               :height height}])})))

(comment

  [:button {:on-click #(prn (.data (deref *vega-view) "costs")
                            (.data (deref *vega-view) "benefits")
                            (.data @*vega-view "lines"))}
   "log data"])
