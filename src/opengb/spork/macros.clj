(ns opengb.spork.macros)

(defmacro read-vega-spec
  "Strip in and parse a Vega spec at compile time. Lets us keep these as JSON
  files for easier loading/testing in the online Vega editor.

  Reads by default from `$ROOT/vega/my-spec-name.vg.json`"
  ([spec-name]
   (read-vega-spec "vega" spec-name))
  ([path spec-name]
   (let [filename (str path "/" spec-name ".vg.json")
         json-string (slurp filename)]
     `(js/JSON.parse ~json-string))))
