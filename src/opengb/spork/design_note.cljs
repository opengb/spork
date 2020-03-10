(ns opengb.spork.design-note
  (:require
   [re-frame.core :refer [subscribe reg-sub reg-event-db]]))

(def ^:private *registered? (atom false))

(defn register-re-frame-handlers
  "Registers `DesignNote` component for use. Usually hidden behind eg.
   `goog.DEBUG` or a boot-time decision.

   Enables re-frame-driven showing/hiding of design notes.
   Provides these re-frame events:

   `[:opengb.spork.design-note/show]`
   `[:opengb.spork.design-note/hide]`
   `[:opengb.spork.design-note/toggle]`

   Provides this re-frame subscription:
   `[:opengb.spork.design-note/showing?]`"
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
           {:style {:color color
                    :border (str "1px solid " color)
                    :padding "0.25em"
                    :margin "0.15em 0"}}
           s])))))
