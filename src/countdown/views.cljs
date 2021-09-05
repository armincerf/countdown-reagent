(ns countdown.views
  (:require
   [re-frame.core :as rf]
   [reagent.core :as r]
   [countdown.events :as events]
   [countdown.routes :as routes]
   [fork.reagent :as fork]
   [countdown.subs :as subs]
   [countdown.db :as db]
   ["/js/helpers" :refer [drawClock]]
   [clojure.string :as str]))

(defn get-real-size
  [el]
  (let [bb (.getBoundingClientRect el)]
    [(.-width bb) (.-height bb)]))

(defn canvas
  [{:keys [width height draw-once]}]
  (rf/dispatch [::events/reset-clock])
  (fn []
    (let [update-size (fn [el timer-left]
                        (when el
                          (let [ctx (.getContext el "2d")]
                            (drawClock ctx timer-left))))]

      (fn [] (let [timer-left     @(rf/subscribe [::subs/timer-left])]
               [:canvas {:ref    #(update-size % timer-left)
                         :id     "canvas"
                         :width  width
                         :height height}])))))

(defn clock
  []
  [:div {:class "flex justify-center"}
   [canvas
    {:width  "250px"
     :height "250px"}]])

(defn timer
  []
  (let [timer-left @(rf/subscribe [::subs/timer-left])]
    [:label.timer
     {:class
      "inline-block border-2 border-black bg-gray-300 px-4 m-4 font-mono"}
     (js/Math.ceil timer-left)]))

(defn tiles
  []
  (let [letters @(rf/subscribe [::subs/board])
        letters-count @(rf/subscribe [::subs/letters-count])
        render-letter (fn [idx letter]
                        [:div.letter
                         {:class (str (when (seq letter) "hover") " tile")
                          :key idx}
                         [:div.flipper
                          [:div.front]
                          (when (>= (count letters) idx)
                            [:div.back letter])]])]
    [:div.board
     [:div.tiles
      [:div.tileBorder
       (map-indexed render-letter
                    (if (seq letters)
                      letters
                      (repeat letters-count "")))]]]))

(defn board
  []
  [:div
   {:class
    " flex flex-col items-center border-blue-800 p-8 border-2 bg-gradient-radial from-yellow-100 to-blue-300"}
   [clock]
   [timer]
   [tiles]])

(defn controls
  []
  (let [running? @(rf/subscribe [::subs/running?])
        time-left @(rf/subscribe [::subs/timer-left])
        board-count @(rf/subscribe [::subs/board-count])
        letters-count @(rf/subscribe [::subs/letters-count])
        reached-vowel-limit? @(rf/subscribe [::subs/vowel-limit?])
        reached-consonant-limit? @(rf/subscribe [::subs/consonant-limit?])
        board-full? (>= board-count letters-count)]
    [:<>
     [:div.letter-buttons
      [:div.flex-1
       [:div.inline-flex
        [:span
         {:class "relative z-0 inline-flex rounded-md"}
         (for [type ["Vowel" "Consonant"]
               :let [vowel? (= type "Vowel")
                     disabled (or board-full?
                                  (if vowel?
                                    reached-vowel-limit?
                                    reached-consonant-limit?))]]
           [:button
            {:disabled disabled
             :key      type
             :class    (if vowel? "groupedBtnL" "groupedBtn")
             :on-click #(rf/dispatch [::events/add-letter vowel?])}
            type])
         [:button
          {:class "groupedBtnR"
           :disabled board-full?
           :on-click #(rf/dispatch [::events/random-fill-letters])}
          "Random Fill"]]]]]
     [:div.timer-buttons
      (when (pos? board-count)
        [:button.btn.btnPrimary
         {:on-click #(rf/dispatch [::events/reset-all])}
         "Reset"])
      (when (and board-full? (= db/game-time
                                time-left))
        [:button.btn.btnPrimary
         {:on-click #(rf/dispatch [::events/start-clock db/game-time])}
         "Start"])
      (when (< time-left db/game-time)
        (if running?
          [:button.btn
           {:on-click #(rf/dispatch [::events/pause-clock])}
           "Pause"]
          [:button.btn
           {:on-click #(rf/dispatch [::events/start-clock])}
           "Resume"]))]]))

(defn word-search
  []
  (let [name "search-term"]
    [fork/form {:initial-values {name ""}}
     (fn search-form
       [{:keys [values handle-change handle-blur]}]
       (let [value (get values name)]
         [:div
          [:input
           {:name name
            :class "input"
            :value value
            :on-change handle-change
            :on-blur handle-blur}]
          [:button.btn.btnPrimary
           {:on-click #(rf/dispatch [::events/lookup-word value])}
           "Check Word"]]))]))

(defn answers
  []
  (let [answers @(rf/subscribe [::subs/results])
        word-result @(rf/subscribe [::subs/check-word-result])
        time-up? @(rf/subscribe [::subs/time-up?])]
    (when time-up?
      [:<>
       [word-search]
       (when word-result
         [:p word-result])
       [:button.btn.btnPrimary
        {:on-click #(rf/dispatch [::events/find-answers])}
        "Show Answers"]
       [:div.answers
        (for [answer answers]
          [:p
           {:key answer}
           answer])]])))

(defn home-panel
  []
  [:div
   {:class "container mx-auto max-w-md flex-col justify-center"}
   [:h1 {:class "text-4xl font-semibold py-4"} "Countdown"]
   [:div
    {:class "w-full items-center"}
    [board]
    [controls]
    [answers]
    [:a {:on-click #(rf/dispatch [::events/navigate :about])}
     "go to Home Page"]]])

(defmethod routes/panels
  :home-panel
  []
  [home-panel])

(defn about-panel []
  [:div
   [:h1 "This is the About Page."]
   [:div
    ]])

(defmethod routes/panels
  :about-panel
  []
  [about-panel])

(defn main-panel
  []
  (let [active-panel (rf/subscribe [::subs/active-panel])]
    (routes/panels @active-panel)))
