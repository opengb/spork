(ns opengb.spork.markdown
  (:require
   [markdown-to-hiccup.core :as md]))

(defn Markdown->Hiccup
  "Renders a markdown string.

   Example: `[Markdown->Hiccup \"* a markdown string *\"]` will be rendered by this
   component into something like `[:strong \"a markdown string\"]`."
  [s]
  (md/component (md/md->hiccup s)))
