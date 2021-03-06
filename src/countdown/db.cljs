(ns countdown.db)

(defn gen-letters
  [letters]
  (flatten
   (for [[letter count] letters]
     (repeat count letter))))

(def vowels (gen-letters
             {"A" 15
              "E" 21
              "I" 13
              "O" 13
              "U" 5}))

(def consonants
  (gen-letters
   {"B" 2
    "C" 3
    "D" 6
    "F" 2
    "G" 3
    "H" 2
    "J" 1
    "K" 1
    "L" 5
    "M" 4
    "N" 8
    "P" 4
    "Q" 1
    "R" 9
    "S" 9
    "T" 9
    "V" 1
    "W" 1
    "X" 1
    "Y" 1
    "Z" 1}))

(def game-time 3)

(def default-db
  {:name "re-frame"
   :vowels vowels
   :letters-count 9
   :numbers-count 6
   :consonants consonants})
