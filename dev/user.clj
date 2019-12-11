(ns user
  (:require
   [figwheel.main.api]
   [taoensso.timbre :as timbre]))

;;; starting up figwheel manually so that we can piggieback into the browser
;;; from cljs files open in the editor
;;;
;;; vim + fireplace.vim
;;; start up repl, do (go) then (cljs-repl)
;;; :Piggieback (figwheel.main.api/repl-env "dev")

(timbre/set-level! :debug)

(defn go
  []
  (let [build-id "dev"]
    (timbre/info (format "starting figwheel with build-id \"%s\"" build-id))
    (figwheel.main.api/start {:mode :serve} build-id)))

(defn halt
  []
  (let [build-id "dev"]
    (timbre/info (format "stopping figwheel for build-id \"%s\"" build-id))
    (figwheel.main.api/stop build-id)))

(defn cljs-repl
  "Provides a primary cljs repl for eg. the terminal or vim fireplace to connect to."
  []
  (let [build-id "dev"]
    (figwheel.main.api/cljs-repl build-id)))
