(ns opengb.spork.keyboard-listener
  (:require
   [reagent.core :as reagent]
   [taoensso.timbre :as timbre]))

(defn make-control-shortcut-handler
  [shortcut-map]
  (fn [ev]
    (let [key-pressed (.-key ev)
          ctrl-key-active? (.-ctrlKey ev)
          tag-name (.-tagName (.-target ev))
          entering-input? (contains? #{"INPUT" "SELECT" "TEXTAREA"}
                                     tag-name)]
      (when (and (not entering-input?)
                 ctrl-key-active?)
        (when-let [action (get-in shortcut-map [key-pressed :action])]
          (action)
          (.preventDefault ev))))))

(defn KeyboardListener
  ""
  [_props _children]
  (let [handler (atom nil)]
    (reagent/create-class
     {:component-did-mount
      (fn layout-ui-component-will-mount
        [component]
        (timbre/debug "adding debug handler")
        (let [props (reagent/props component)
              shortcut-map (:shortcuts-map props)]
          (reset! handler (make-control-shortcut-handler shortcut-map)))
        (js/window.addEventListener "keydown" @handler))
      :component-will-unmount
      (fn layout-ui-component-will-unmount
        [_component]
        (timbre/debug "removing debug handler")
        (js/window.removeEventListener "keydown" @handler))
      :reagent-render
      (fn [_props children]
        [:div.keyboard-shortcuts children])})))
