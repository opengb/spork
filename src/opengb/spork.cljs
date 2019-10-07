(ns opengb.spork
  (:require
   [opengb.spork.quantity :as qty]
   [opengb.spork.design-note :as design-note]
   [opengb.spork.keyboard-listener :as kb]
   [opengb.spork.leaflet :as leaflet]
   [opengb.spork.vega :as vega]
   [opengb.spork.virtualized :as virtualized]))

(def Map leaflet/Map)

(def Chart vega/Chart)

(def AutoSizer virtualized/AutoSizer)

(def StretchHeight virtualized/AutoHeighter)

(def StretchWidth virtualized/AutoWidther)

(def StretchHeightWidth virtualized/AutoSizer2)

(def KeyboardListener kb/KeyboardListener)

(def Quantity qty/Quantity)

(def DesignNote design-note/DesignNote)

(def init-design-notes
  "Enables re-frame-driven showing/hiding of design notes.
   Provides these re-frame events:

   `[:opengb.spork.design-note/show]`
   `[:opengb.spork.design-note/hide]`
   `[:opengb.spork.design-note/toggle]`

   Provides this re-frame subscription:
   `[:opengb.spork.design-note/showing?]`"
  design-note/register)
