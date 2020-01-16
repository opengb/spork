(ns opengb.spork
  (:require
   [opengb.spork.design-note :as design-note]
   [opengb.spork.error-boundary :as error-boundary]
   [opengb.spork.keyboard-listener :as kb]
   [opengb.spork.leaflet :as leaflet]
   [opengb.spork.markdown :as markdown]
   [opengb.spork.quantity :as qty]
   [opengb.spork.vega :as vega]
   [opengb.spork.virtualized :as virtualized]))

(def ^:deprecated Map leaflet/CircleMarkerMap)

(def CircleMarkerMap leaflet/CircleMarkerMap)

(def Chart vega/Chart)

(def AutoSizer virtualized/AutoSizer)

(def StretchHeight virtualized/AutoHeighter)

(def StretchWidth virtualized/AutoWidther)

(def StretchHeightWidth virtualized/AutoSizer2)

(def KeyboardListener kb/KeyboardListener)

(def Quantity qty/Quantity)

(def DesignNote design-note/DesignNote)

(def ^:deprecated init-design-notes
  "[]
   Enables re-frame-driven showing/hiding of design notes.
   Provides these re-frame events:

   `[:opengb.spork.design-note/show]`
   `[:opengb.spork.design-note/hide]`
   `[:opengb.spork.design-note/toggle]`

   Provides this re-frame subscription:
   `[:opengb.spork.design-note/showing?]`"
  design-note/register-re-frame-handlers)

(def ^:deprecated Markdown markdown/Markdown->Hiccup)

(def Markdown->Hiccup markdown/Markdown->Hiccup)

(def ErrorBoundary error-boundary/ErrorBoundary)

(def ^:private get-rf-initializer
  {:design-notes design-note/register-re-frame-handlers
   :leaflet      leaflet/register-re-frame-handlers})

(defn register-re-frame-handlers
  "Registers some or all components's re-frame handlers.

   Supply an optional seq of component keywords to limit the initialization
   to only those components."
  ([]
   (register-re-frame-handlers (keys get-rf-initializer)))
  ([component-keys]
   (let [valid-key? (set (keys get-rf-initializer))]
   (doseq [k (filter valid-key? component-keys)
           :let [handler (get-rf-initializer k)]]
     (handler)))))
