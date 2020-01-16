(ns opengb.spork
  (:require
   [markdown-to-hiccup.core :as md]
   [opengb.spork.design-note :as design-note]
   [opengb.spork.error-boundary :as error-boundary]
   [opengb.spork.keyboard-listener :as kb]
   [opengb.spork.leaflet :as leaflet]
   [opengb.spork.quantity :as qty]
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

(def init-map
  "[uri]

   Enables re-frame integration for pulling leaflet config from a supplied
   endpoint.

   Provides this re-frame event:

   `[:opengb.spork.leaflet/request-tile-config]`

   Provides this re-frame subscription:
   `[:opengb.spork.leaflet/tile-config]`"
  leaflet/register-re-frame)

(defn Markdown
  "Renders a markdown string.

   Example: `[Markdown \"* a markdown string *\"]` will be rendered by this
   component into something like `[:strong \"a markdown string\"]`."
  [s]
  (md/component (md/md->hiccup s)))

(def ErrorBoundary error-boundary/ErrorBoundary)
