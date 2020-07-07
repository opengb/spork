(ns opengb.spork.dark-launch
  (:require
   [opengb.spork.containers :as containers]
   [re-frame.core :refer [dispatch subscribe reg-sub reg-event-db]]))

(def ^:private *registered? (atom false))

(defn register-re-frame-handlers
  "Registers dark-launch components for use.

   Enables re-frame-driven toggling of the dark-launch configuration
   panel, as well as each feature wrapped in an `If` or `When` component.

   Provides these re-frame event handlers:

   `[:opengb.spork.dark-launch/show-panel]`
   `[:opengb.spork.dark-launch/hide-panel]`
   `[:opengb.spork.dark-launch/register]`
   `[:opengb.spork.dark-launch/enable]`
   `[:opengb.spork.dark-launch/disable]`

   Provides these re-frame subscriptions:
   `[:opengb.spork.dark-launch/show-panel?]`
   `[:opengb.spork.dark-launch/features]`
   `[:opengb.spork.dark-launch/feature-states]`
   `[:opengb.spork.dark-launch/enabled?]`"
  []
  ;; subs
  (reg-sub
   ::show-panel?
   (fn [db] (::show-panel db)))

  (reg-sub
   ::features
   (fn [db _] (::features db)))

  (reg-sub
   ::feature-states
   (fn [db _] (::feature-states db)))

  (reg-sub
   ::enabled?
   (fn [db [_ feature-key]]
     (get-in db [::feature-states feature-key] false)))

  ;; events
  (reg-event-db
   ::show-panel
   (fn [db _] (assoc db ::show-panel true)))

  (reg-event-db
   ::hide-panel
   (fn [db _] (dissoc db ::show-panel)))

  (reg-event-db
   ::register
   (fn [db [_ feature-key]]
     (update db ::features (fnil conj (sorted-set)) feature-key)))

  (reg-event-db
   ::enable
   (fn [db [_ feature-key]]
     (assoc-in db [::feature-states feature-key] true)))

  (reg-event-db
   ::disable
   (fn [db [_ feature-key]]
     (assoc-in db [::feature-states feature-key] false)))

  (reset! *registered? true))

;; * Views

(defn When
  "Wraps one component and ties the UI to the state of the flipper."
  [feature-key _]
  (let [*enabled? (subscribe [::enabled? feature-key])]
    (fn [_ when-enabled]
      (when @*enabled? when-enabled))))

(defn If
  "Wraps two components and ties the UI to the state of the flipper."
  [feature-key _ _]
  (let [*enabled? (subscribe [::enabled? feature-key])]
    (fn [_ when-enabled when-disabled]
      (if @*enabled? when-enabled when-disabled))))

(defn Panel
  "Provides a modal for enabling/disabling known UI flippers."
  []
  (let [*showing?       (subscribe [::show-panel?])
        *features       (subscribe [::features])
        *feature-states (subscribe [::feature-states])
        on-close        #(dispatch [::hide-panel])]
    (fn []
      [containers/Modal
       {:backdrop-on-click on-close
        :show?             @*showing?
        :wrap-nicely?      true}
       [:div {:class "dark-launch-panel"}
        [:p {:class "dark-launch-panel-title"
             :style {:font-size     "1.5rem"
                     :line-height   "1.3rem"
                     :font-weight   "bold"
                     :border-bottom "1px solid #dedede"}}
         "Dark Launch"]
        [:div {:class "dark-launch-panel-features-list"}
         (doall (for [k    @*features
                      :let [enabled? (k @*feature-states)]]
                  ^{:key k}
                  [:div
                   [:label
                    [:input
                     {:type    "checkbox"
                      :checked enabled?
                      :on-click
                      #(dispatch [(if enabled? ::disable ::enable) k])}]
                    [:span (str k)]]]))]]])))
