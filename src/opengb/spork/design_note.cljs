(ns opengb.spork.design-note
  (:require
   [re-frame.core :refer [subscribe reg-sub reg-event-db]]
   [stylefy.core :refer [use-style]]))

(def ^:private *registered? (atom false))

(defn register
  "Registers `DesignNote` component for use. Usually hidden behind eg.
   `goog.DEBUG` or a boot-time decision."
  []
  (reg-sub
   ::showing?
   (fn [db _]
     (::showing? db)))

  (reg-event-db
   ::show
   (fn [db _]
     (assoc db ::showing? true)))

  (reg-event-db
   ::hide
   (fn [db _]
     (assoc db ::showing? false)))

  (reg-event-db
   ::toggle
   (fn [db _]
     (update db ::showing? not)))

  (reset! *registered? true))

(defn DesignNote
  "Annotates the site such that we can mark it up for dev, but hide the notes by
   default and expose them with re-frame events. Only takes effect if `register`
   has been invoked."
  [_s]
  (when @*registered?
    (let [*show? (subscribe [::showing?])
          color "#BC03FF"]
      (fn [s]
        (when @*show?
          [:div.design-note
           (use-style {:color color
                       :border (str "1px solid " color)
                       :padding "0.25em"
                       :margin "0.15em 0"})
           s])))))
