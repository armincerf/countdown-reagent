#!/usr/bin/env bb
(ns gen-dictionary
  (:require [clojure.string :as str]
            [clojure.java.io :as io]))

(defn- reader->words [rdr]
  (let [single-word? #(nil? (re-find #"[^a-z]" %))
        max-nine-letters? #(<= (count %) 9)
        min-three-letters? #(>= (count %) 3)]
    (->> (line-seq rdr)
         (map str/lower-case)
         (filter single-word?)
         (filter min-three-letters?)
         (filter max-nine-letters?))))

(defn generate-dictionary
  [dictionary add-word filename]
  (with-open [rdr (io/reader filename)]
    (reduce add-word dictionary (reader->words rdr))))

(defn- word->key [word]
  (conj (vec (sort word)) :words))

(defn- add-to-trie [trie word]
  (let [key (word->key word)
        words (get-in trie key #{})]
    (assoc-in trie key (conj words word))))

(def dictionary (generate-dictionary {} add-to-trie "/usr/share/dict/words"))
(spit "resources/public/dictionary.edn" dictionary)
