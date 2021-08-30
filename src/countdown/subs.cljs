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
   (or (:time-left db) 30)))

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
