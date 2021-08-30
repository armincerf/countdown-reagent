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
     (prn "update")
     (if (pos? time)
       {:db (assoc db :time-left (- time 0.1))}
       {:fx [[:dispatch [::reset-clock]]]}))))

(rf/reg-event-fx
 ::start-clock
 (fn [{:keys [db]} [_ time-left]]
   (let [interval (js/setInterval #(rf/dispatch [::update-clock]) 100)]
     {:fx [[:clear-interval (:interval db)]]
      :db (-> db
              (assoc :time-left (or time-left (:time-left db)))
              (assoc :interval interval))})))

(rf/reg-event-fx
 ::pause-clock
 (fn [{:keys [db]} _]
   {:fx [[:clear-interval (:interval db)]]
    :db (dissoc db :interval)}))

(rf/reg-event-fx
 ::reset-all
 (fn [{:keys [db]} _]
   {:fx [[:dispatch [::reset-clock]]]
    :db (assoc db :board [])}))

(rf/reg-event-fx
 ::reset-clock
 (fn [{:keys [db]} _]
   (def db db)
   {:fx [[:clear-interval (:interval db)]]
    :db (-> db
            (dissoc :interval)
            (dissoc :time-left))}))

(rf/reg-event-fx
 ::add-letter
 (fn [{:keys [db]} [_ vowel?]]
   (let [{:keys [vowels consonants board letters-count]} db
         shuffled-letters            (shuffle (if vowel? vowels consonants))
         chosen-letter               (first shuffled-letters)
         remaining-letters           (pop shuffled-letters)
         board (remove empty? board)
         board-count (count board)
         missing-tiles (if (< board-count letters-count)
                         (repeat (- letters-count board-count 1) "")
                         [])]
     {:db (-> db
              (assoc (if vowel? :vowels :consonants) remaining-letters
                     :board (concat board [chosen-letter] missing-tiles)))})))

(rf/reg-event-fx
 ::random-fill-letters
 (fn [{:keys [db]} _]
   (let [board (:board db)]
     (when (or (nil? board) (< (count (remove empty? board)) 9))
       {:fx [[:dispatch [::add-letter (rand-nth [true false])]]
             [:dispatch-later {:ms       100
                               :dispatch [::random-fill-letters]}]]}))))

(comment
  (rf/dispatch [::start-clock 30])
  (rf/dispatch [::reset-clock]))

(rf/reg-fx
 :clear-interval
 (fn [interval]
   (def interval interval)
   (js/clearInterval interval)))
