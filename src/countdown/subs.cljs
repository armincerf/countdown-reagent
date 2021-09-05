(ns countdown.subs
  (:require
   [countdown.db :as db]
   [clojure.string :as str]
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
   (or (:time-left db) db/game-time)))

(rf/reg-sub
 ::running?
 (fn [db _]
   (and (:time-left db) (:interval db))))

(rf/reg-sub
 ::can-start?
 (fn [db _]
   true))

(rf/reg-sub
 ::letters
 (fn [db _]
   ["A" "B" "C" "E"]))

(rf/reg-sub
 ::board
 (fn [db _]
   (:board db)))

(rf/reg-sub
 ::board-count
 :<- [::board]
 (fn [board]
   (count (remove empty? board))))

(rf/reg-sub
 ::letters-count
 (fn [db _]
   (:letters-count db)))

(defn is-vowel?
  [letter]
  (some #(= % (str/upper-case letter)) db/vowels))

(rf/reg-sub
 ::vowel-limit?
 :<- [::board]
 :<- [::letters-count]
 (fn [[board letters-count]]
   (let [max-allowed (- letters-count
                        (js/Math.floor
                         (/ letters-count 2)))]
     (= max-allowed
        (count (filter is-vowel? board))))))

(defn is-consonant?
  [letter]
  (some #(= % (str/upper-case letter)) db/consonants))

(rf/reg-sub
 ::consonant-limit?
 :<- [::board]
 :<- [::letters-count]
 (fn [[board letters-count]]
   (let [max-allowed (- letters-count
                        (js/Math.floor
                         (/ letters-count 3)))]
     (= max-allowed
        (count (filter is-consonant? board))))))

(rf/reg-sub
 ::numbers-count
 (fn [db _]
   (:numbers-count db)))

(rf/reg-sub
 ::check-word-result
 (fn [db _]
   (when-let [word (:word db)]
     (case (:check-word-results db)
       :valid
       (str "Nice one! " word " is in the dictionary and gets you " (count word) " points.")
       :bad-letters
       (str "you may want to read the board again...")
       :bad-word
       (str "Sorry, " word " isn't in our dictionary")
       nil))))

(rf/reg-sub
 ::results
 (fn [db _]
   (:answers db)))

(rf/reg-sub
 ::time-up?
 (fn [db _]
   (:time-up? db)))
