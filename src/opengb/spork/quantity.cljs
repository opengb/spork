(ns opengb.spork.quantity
  (:require
   [goog.i18n.NumberFormat.Format]
   [opengb.dram.quantity]
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

(defn Unit
  [k]
  [:span.unit (use-style {:margin-left    "0.25em"
                          :text-transform "none !important"
                          :opacity        0.4})
   (opengb.dram.quantity/unit->ui-string k)])

(defn Quantity
  [[mag unit]]
  ;; let's swap/display both systems with a subscription
  ;; this is better than the hide-with-CSS approach since we may never need to
  ;; calc the opposing system
  ;; TODO replace with a subscription
  (let [_systems (reagent/atom {:metric? true :imperial? false})]
    [:span.qty [Mag mag] [Unit unit]]))
