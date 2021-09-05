(ns countdown.events
  (:require
   [clojure.set :as set]
   [re-frame.core :as rf]
   [superstructor.re-frame.fetch-fx]
   [clojure.edn :as edn]
   [countdown.db :as db]
   [clojure.string :as str]))

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
 ::time-up
 (fn [{:keys [db]} _]
   {:fx [[:dispatch [::fetch-dictionary]]]
    :db (assoc db :time-up? true)}))

(rf/reg-event-fx
 ::update-clock
 (fn [{:keys [db]} _]
   (let [time (:time-left db)]
     (if (pos? time)
       {:db (assoc db :time-left (- time 0.1))}
       {:fx [[:dispatch [::time-up]]]}))))

(rf/reg-event-fx
 ::fetch-dictionary-success
 (fn [{:keys [db]} [_ response]]
   {:db (assoc db :dictionary
               (edn/read-string (:body response)))}))

(rf/reg-event-fx
 ::http-fail
 (fn [_ [_ res]]
   (prn res)))

(rf/reg-event-fx
 ::fetch-dictionary
 (fn [{:keys [db]} _]
   (when-not (:dictionary db)
     {:fetch {:method :get
              :url "/dictionary.edn"
              :cache :force-cache
              :headers {"Accept" "application/edn"}
              :on-success [::fetch-dictionary-success]
              :on-failure [::http-fail]}})))

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
    :db (dissoc db
                :board
                :check-word-results)}))

(rf/reg-event-fx
 ::reset-clock
 (fn [{:keys [db]} _]
   {:fx [[:clear-interval (:interval db)]]
    :db (dissoc db
                :time-up?
                :interval
                :time-left)}))

(rf/reg-event-fx
 ::add-letter
 (fn [{:keys [db]} [_ vowel?]]
   (let [{:keys [vowels consonants board letters-count]} db
         shuffled-letters (shuffle (if vowel? vowels consonants))
         chosen-letter (first shuffled-letters)
         remaining-letters (pop shuffled-letters)
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
             [:dispatch-later {:ms 100
                               :dispatch [::random-fill-letters]}]]}))))

(comment
  (rf/dispatch [::start-clock db/game-time])
  (rf/dispatch [::reset-clock]))

(rf/reg-fx
 :clear-interval
 (fn [interval]
   (js/clearInterval interval)))

(defn- word->key
  [word]
  (conj (vec (sort word)) :words))

(defn trie-anagrams
  [dictionary word]
  (get-in dictionary (word->key word) #{}))

(defn- power-set [word]
  (let [add-char-seqs #(set/union %1 (set (map (partial str %2) %1)))]
    (reduce add-char-seqs #{""} word)))

(defn word->sub-anagrams
  [dictionary word]
  (->> word
       str/lower-case
       power-set
       (map #(trie-anagrams dictionary %))
       (apply set/union)
       (sort #(> (count %1) (count %2)))
       (take 10)))

(defn sufficient-letters?
  [board word]
  (let [board-freqs (frequencies board)
        word-freqs (frequencies word)]
    (every? #(>= (get board-freqs %)
               (get word-freqs %))
          word)))

(rf/reg-event-db
 ::lookup-word
 (fn [db [_ word]]
   (let [results (trie-anagrams (:dictionary db) word)
         in-dictionary? (results word)
         sufficient-letters? (sufficient-letters?
                              (str/lower-case
                               (str/join (:board db))) word)]
     (assoc db
            :check-word-results (cond
                                  (not sufficient-letters?)
                                  :bad-letters
                                  (not in-dictionary?)
                                  :bad-word
                                  :else
                                  :valid)
            :word word))))

(rf/reg-event-db
 ::find-answers
 (fn [db _]
   (let [results (word->sub-anagrams
                  (:dictionary db)
                  (str/join (:board db)))]
     (assoc db :answers results))))
