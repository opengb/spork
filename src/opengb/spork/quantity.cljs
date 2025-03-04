(ns opengb.spork.quantity
  (:require
   [goog.i18n.NumberFormat.Format]
   [opengb.dram.quantity]
   [reagent.core :as reagent])
  (:import (goog.i18n NumberFormat)
           (goog.i18n.NumberFormat Format)))

; https://gist.github.com/zentrope/181d591b52dcf3f5d336bc15131a1116
(def number-format (NumberFormat. Format/DECIMAL))
(def metric-format (.setSignificantDigits (NumberFormat. "#,##0.##") 2))
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
  [:span.unit {:style {:margin-left    "0.25em !important"
                       :text-transform "none !important"
                       :opacity        "0.4 !important"}}
   (opengb.dram.quantity/unit->ui-string k)])

(defn Quantity
  [[mag unit]]
  ;; let's swap/display both systems with a subscription
  ;; this is better than the hide-with-CSS approach since we may never need to
  ;; calc the opposing system
  ;; TODO replace with a subscription
  (let [_systems (reagent/atom {:metric? true :imperial? false})]
    [:span.qty [Mag mag] [Unit unit]]))
