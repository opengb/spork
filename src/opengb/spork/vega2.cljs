(ns opengb.spork.vega2
  "Version 2 of the vega renderer.

   Presumes that `vega` has been attached to `js/window` by other means, eg. a
   `<script>` tag or another cljs module. This seems to improve advanced
   compilation reliability and bundle sizes."
  (:require
   [reagent.core :as reagent]))

;; Source: https://github.com/d3/d3-time-format/blob/main/locale/en-CA.json
(def locale:en-CA
  {:time
   {:dateTime "%a %b %e %X %Y",
    :date "%Y-%m-%d",
    :time "%H:%M:%S",
    :periods ["AM" "PM"],
    :days ["Sunday" "Monday" "Tuesday" "Wednesday" "Thursday" "Friday" "Saturday"],
    :shortDays ["Sun" "Mon" "Tue" "Wed" "Thu" "Fri" "Sat"],
    :months ["January" "February" "March" "April" "May" "June" "July" "August" "September" "October" "November" "December"],
    :shortMonths ["Jan" "Feb" "Mar" "Apr" "May" "Jun" "Jul" "Aug" "Sep" "Oct" "Nov" "Dec"]}
   :number
   {:decimal "."
    :thousands ","
    :grouping [3]}})

;; Source: https://github.com/d3/d3-time-format/blob/main/locale/fr-CA.json
(def locale:fr-CA
  {:time
   {:dateTime "%a %e %b %Y %X",
    :date "%Y-%m-%d",
    :time "%H:%M:%S",
    :periods ["" ""],
    :days ["dimanche" "lundi" "mardi" "mercredi" "jeudi" "vendredi" "samedi"],
    :shortDays ["dim" "lun" "mar" "mer" "jeu" "ven" "sam"],
    :months ["janvier" "février" "mars" "avril" "mai" "juin" "juillet" "août" "septembre" "octobre" "novembre" "décembre"],
    :shortMonths ["jan" "fév" "mar" "avr" "mai" "jui" "jul" "aoû" "sep" "oct" "nov" "déc"]}
   :number
   {:decimal ","
    :thousands " "
    :grouping [3]}})

(def locale-key->locale-map
  {"en-CA" locale:en-CA
   "fr-CA" locale:fr-CA})

(defn update-component
  [^js/vega.View vega-view this-component]
  (let [props (reagent/props this-component)
        {:keys [width height data signals debug]} props]
    ;; update size
    (.signal vega-view "height" height)
    (.signal vega-view "width" width)

    (doall ;; completely replace datasets; could do better here
     (for [[dataset-name ms] data]
       (.change vega-view (name dataset-name)
                (-> js/vega ;; advanced compilation??
                    (.changeset)
                    (.remove (constantly true))
                    (.insert (clj->js ms))))))

    (doall ;; update signals
     (for [[signal-name x] signals]
       (.signal vega-view (name signal-name)
                (clj->js x))))
    (.run vega-view)

    ;; log requested datasets for debugging
    (doseq [dataset-name debug]
      (js/console.debug dataset-name (.data vega-view dataset-name)))))

(defn mount-component
  [a-ref this-component]
  (let [props          (reagent/props this-component)
        vega-spec      (clj->js (or (:vega-spec props) {}))
        vega-config    (->> (get props :locale "en-CA")
                            locale-key->locale-map
                            (hash-map :locale)
                            clj->js)
        runtime        (.parse js/vega vega-spec vega-config)
        view           (-> (js/vega.View. runtime)
                           (.initialize a-ref)
                           (.renderer "svg")
                           (.hover))
        on-click       (:on-click props)
        clickable-view (if (ifn? on-click)
                         (.addEventListener view "click" #(on-click %))
                         view)]
    ;; uncomment to debug in signal expressions
    ;; see https://vega.github.io/vega/docs/expressions/#debug
    ; (.logLevel view js/vega.Warn)
    (.run clickable-view)
    view))

(defn VegaRenderer
  "Render component for a generic vega chart."
  [_props]
  (let [*a-ref     (atom nil)
        *vega-view (atom nil)]
    (reagent/create-class
     {:component-did-mount
      (fn did-mount [this] (let [view (mount-component @*a-ref this)]
                             (reset! *vega-view view)
                             ;; give it one initial bump, in case of static data
                             (update-component @*vega-view this)))

      :component-did-update
      (fn did-update [this _old-argv] (update-component @*vega-view this))

      :reagent-render
      (fn render [{:keys [width height] :as _props}]
        [:div {:ref #(reset! *a-ref %)
               :width width
               :height height}])})))
