(ns opengb.spork.dark-launch-cards
  (:require
   [devcards.core :refer [defcard-rg]]
   [opengb.spork :as spork]
   [opengb.spork.dark-launch :as dark-launch]
   [re-frame.core :as re-frame :refer [dispatch subscribe]]
   [reagent.core :as reagent]))

(defn- register-demo-features
  []
  (let [features #{:johndoe/shiny-new-feature
                   :janedeer/improved-widgets}]
    (doseq [feature features]
      (dispatch [::dark-launch/register feature]))))

(defcard-rg ReFrameConfigFeaturesPanel
  (fn [*registered?]
    (when-not *registered?
      (spork/register-re-frame-handlers [:dark-launch])
      (register-demo-features)
      (reset! *registered? true))
    [:div
     [:button {:on-click #(dispatch [::dark-launch/show-panel])}
      "Show features panel"]
     [dark-launch/Panel]])
  (reagent/atom false))
