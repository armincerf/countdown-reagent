(ns countdown.subs
  (:require
   [re-frame.core :as rf]))

(rf/reg-sub
 ::name
 (fn [db]
   (:name db)))

(rf/reg-sub
 ::active-panel
 (fn [db _]
   (:active-panel db)))

(rf/reg-sub
 ::timer-left
 (fn [db _]
   (or (:time-left db) 30)))

(rf/reg-sub
 ::running?
 (fn [db _]
   (:interval db)))

(rf/reg-sub
 ::can-start?
 (fn [db _]
   true))

(rf/reg-sub
 ::letters
 (fn [db _]
   ["A" "B" "C" "E"]))
