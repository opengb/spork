(ns opengb.spork.quantity
  (:require
   [goog.i18n.NumberFormat.Format]
   [reagent.core :as reagent]
   [stylefy.core :refer [use-style]])
  (:import (goog.i18n NumberFormat)
           (goog.i18n.NumberFormat Format)
           [goog.async Throttle]))

; https://gist.github.com/zentrope/181d591b52dcf3f5d336bc15131a1116
(def number-format (NumberFormat. Format/DECIMAL))
(def metric-format (NumberFormat. "#,##0"))
(def currency-format (NumberFormat. Format/CURRENCY))
(def compact-currency-format (NumberFormat. "¤ #,##0"))
(def html-currency-format (NumberFormat. "#,##0"))
(def fuzzy-currency-format (NumberFormat. Format/COMPACT_LONG))

(defn format-as-number
  [num]
  (.format metric-format (str num)))

(defn Mag
  [n]
  [:span.mag [:strong (format-as-number n)]])

(defn unit->ui-string
  "Formats the `unit` for display to a user."
  [unit]
  (let [cs {:mi2            "sq mi"
            :ft2            "sq ft"
            :ft2_a          "ft²/year"
            :USD_ft2_a      "USD/ft²/year"
            :m2             "m²"
            :GJ_m2_a        "GJ/m²/year"
            :kWh_m2_a       "kWh/m²/year"
            :tCO2e_a        "tCO₂e/year"
            "m**2"          "m²"
            "kWh/m**2/year" "kWh/m²"
            "kg/m**2/year"  "kg/m²"
            "l/m**2/year"   "l/m²"
            }]
    (get cs unit unit)))

(defn Unit
  [k]
  [:span.unit (use-style {:margin-left    "0.25em"
                          :text-transform "none !important"
                          :opacity        0.4})
   (unit->ui-string k)])

(defn Quantity
  [[mag unit]]
  ;; let's swap/display both systems with a subscription
  ;; this is better than the hide-with-CSS approach since we may never need to
  ;; calc the opposing system
  ;; TODO replace with a subscription
  (let [_systems (reagent/atom {:metric? true :imperial? false})]
    [:span.qty [Mag mag] [Unit unit]]))
