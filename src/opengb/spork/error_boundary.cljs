(ns opengb.spork.error-boundary
  (:require
   [reagent.core :as reagent]))

(defn ErrorBoundary
  "Catches render errors so that the entire UI doesn't go away.

   See https://lilac.town/writing/modern-react-in-cljs-error-boundaries/"
  [& _children]
  (let [*err-state (reagent/atom nil)]
    (reagent/create-class
     {:display-name "ErrorBoundary"
      :component-did-catch
      (fn [err info]
        (reset! *err-state [err info]))
      :reagent-render
      (fn [& children]
        (if (nil? @*err-state)
          (into [:<>] children)
          (let [[_ info] @*err-state]
            [:div {:style {:color "white"
                           :background-color "rgba(255, 0, 0, 1.0)"}}
             [:div "Rendering error!"]
             [:code (pr-str info)]])))})))
