(ns countdown.views
  (:require
   [re-frame.core :as rf]
   [reagent.core :as r]
   [countdown.events :as events]
   [countdown.routes :as routes]
   [countdown.subs :as subs]
   ["/js/helpers" :refer [drawClock]]
   [clojure.string :as str]))

(defn get-real-size [el]
  (let [bb (.getBoundingClientRect el)]
    [(.-width bb) (.-height bb)]))

(defn canvas [{:keys [width height draw-once]}]
  (let [state (atom {:size [250 250]})]
    (r/create-class
     {:reagent-render
      (fn []
        (let [update-size (fn [el timer-left]
                            (when el
                              (let [size (get-real-size el)
                                    ctx  (.getContext el "2d")]
                                (swap! state assoc :size size)
                                (drawClock ctx timer-left))))]

          (fn [] (let [{:keys [size]} @state
                       timer-left @(rf/subscribe [::subs/timer-left])
                       [w h] size]
                   [:canvas {:ref    #(update-size % timer-left)
                             :id "canvas"
                             :width w
                             :height h}]))))
      :component-did-mount (fn [] (reset! state {:size nil}))})))

(defn clock
  []
  (let [running?   @(rf/subscribe [::subs/running?])
        can-start? @(rf/subscribe [::subs/can-start?])]
    [:div {:class "flex justify-center"}
     [canvas
      {:draw-once (and (not running?) can-start?)
       :width     "250px"
       :height    "250px"}]]))

(defn timer
  []
  (let [timer-left @(rf/subscribe [::subs/timer-left])]
    [:label.timer
     {:class
      "inline-block border-2 border-black bg-gray-300 px-4 m-4 font-mono"}
     (js/Math.ceil timer-left)]))

(defn tiles
  []
  (let [letters @(rf/subscribe [::subs/letters])]
    [:div.board
     [:div.tiles
      [:div.tileBorder
       (for [letter letters]
         [:div.letter
          {:key letter}
          [:div.flipper
           [:div.front]
           [:div.back
            letter]]])]]]))

(defn board
  []
  [:div
   {:class
    "flex flex-col items-center border-blue-800 p-8 border-2 bg-gradient-radial from-yellow-100 to-blue-300"}
   [clock]
   [timer]
   [tiles]])

(defn controls
  []
  (let [running? @(rf/subscribe [::subs/running?])]
    [:div
     [:button.btn.btnPrimary
      {:on-click #(rf/dispatch [::events/start-clock 30])}
      "Start"]]))

(defn home-panel []
  [:div
   {:class "container mx-auto max-w-md flex-col justify-center"}
   [:h1 {:class "text-4xl font-semibold py-4"} "Countdown"]
   [:div
    {:class "w-full items-center"}
    [board]
    [controls]]])

(defmethod routes/panels :home-panel [] [home-panel])

(defn about-panel []
  [:div
   [:h1 "This is the About Page."]

   [:div
    [:a {:on-click #(rf/dispatch [::events/navigate :home])}
     "go to Home Page"]]])

(defmethod routes/panels :about-panel [] [about-panel])

(defn main-panel []
  (let [active-panel (rf/subscribe [::subs/active-panel])]
    (routes/panels @active-panel)))
