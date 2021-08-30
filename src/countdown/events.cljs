(ns countdown.events
  (:require
   [re-frame.core :as rf]
   [countdown.db :as db]))

(rf/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(rf/reg-event-fx
  ::navigate
  (fn [_ [_ handler]]
   {:navigate handler}))

(rf/reg-event-fx
 ::set-active-panel
 (fn [{:keys [db]} [_ active-panel]]
   {:db (assoc db :active-panel active-panel)}))

(rf/reg-event-fx
 ::update-clock
 (fn [{:keys [db]} _]
   (let [time (:time-left db)]
     (if (pos? time)
       {:db (assoc db :time-left (- time 0.1))}
       {:fx [[:dispatch [::reset-clock]]]}))))

(rf/reg-event-fx
 ::start-clock
 (fn [{:keys [db]} [_ time-left]]
   (let [interval (js/setInterval #(rf/dispatch [::update-clock]) 100)]
     {:db (-> db
              (assoc :time-left time-left)
              (assoc :interval interval))})))

(rf/reg-event-fx
 ::pause-clock
 (fn [{:keys [db]} _]
   {:fx [[:clear-interval (:interval db)]]
    :db (dissoc db :interval)}))

(rf/reg-event-fx
 ::reset-clock
 (fn [{:keys [db]} _]
   (def db db)
   {:fx [[:clear-interval (:interval db)]]
    :db (-> db
            (dissoc :interval)
            (dissoc :time-left))}))

(comment
  (rf/dispatch [::start-clock 30])
  (rf/dispatch [::reset-clock]))

(rf/reg-fx
 :clear-interval
 (fn [interval]
   (def interval interval)
   (js/clearInterval interval)))
