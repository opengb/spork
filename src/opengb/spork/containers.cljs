(ns opengb.spork.containers)

(defn Modal
  "Displays a modal that is horizontally and vertically centered on the screen.
  Behind the modal is a transparent backdrop that prevents the user
  from interacting with components outside the modal.

  Inspired by re-com's modal-panel (https://github.com/day8/re-com/blob/master/src/re_com/modal_panel.cljs)"
  [{:keys [backdrop-on-click show? wrap-nicely?]} & children]
  (when show?
    [:div
     {:class "spork-modal"
      :style {:position "fixed"
              :left     0
              :top      0
              :width    "100%"
              :height   "100%"
              :z-index  1000}}
     [:div {:class    "spork-modal-backdrop"
            :style    {:position         "fixed"
                       :width            "100%"
                       :height           "100%"
                       :background-color "black"
                       :opacity          0.6
                       :z-index          1}
            :on-click backdrop-on-click}]
     [:div {:class "spork-modal-children"
            :style (merge {:position "relative"
                           :z-index  2
                           :margin   "10vh auto"
                           :width    "80vw"
                           :height   "80vh"}
                          (when wrap-nicely?
                            {:background-color "white"
                             :padding          "16px"
                             :border-radius    "6px"}))}
      children]]))
