(ns liquidfun-experiment.core
  (:require [clojure.core.async :as async]
            [clojure.math.numeric-tower :as math]
            [quil.core :as q :include-macros true]
            [quil.middleware :as m]
            [org.nfrac.liquidfun.core :as lf]))


;; ---------- 

(defn init-world [n]
  (let [world (lf/new-world)
        shape1 (lf/polygon [[-4 -1] [4 -1] [4 0] [-4 0]])
        shape2 (lf/polygon [[-4 -0.1] [-2 -0.1] [-2 2] [-4 2]])
        shape3 (lf/polygon [[2 -0.1] [4 -0.1] [4 2] [2 2]])
        ground (body! world {:type :static}
                      {:shape shape1}
                      {:shape shape2}
                      {:shape shape3})
        ps (lf/particle-system! world {:radius 0.035}
                             {:shape (lf/circle 2 [0 3])
                              :color [255 0 0 255]})
        cir (lf/body! world {:position [0 8]}
                   {:shape (lf/circle 0.5)
                    :density 0.5})] ))
;; ---------- Driver ---------------

(defn update-particles [state] 
  (into state (iterate-particle-sim (:particles state))))

(defn run-sim [state] 
  (loop [] 
    (let [st @state]
    (when (not (:stop st))
      ;(print ".")
      (swap! state update-particles)
      (recur)))))

;; ---------- QUIL Viz -------------


(defn draw [state]
  (q/background 0)
  (q/no-fill)
;  (q/stroke 0 0 255)
;  (doseq [p (vals (:particles state))]
;    (q/ellipse (px p) (py p) D D))
  (q/stroke 0 255 0)
  (doseq [[p1 p2] (:collisions state)]
    (q/line (px p1) (py p1) (px p2) (py p2)))

  ;;(q/stroke 0 0 255)
;;  (doseq [[v b] (:buckets state)]
;;    (q/stroke 0 v 255)
;;    (doseq [p b]
;;      (q/ellipse (px p) (py p) D D))))
;;(q/save-frame "frame#####.png")
)


(defn start-sketch [state]
  (q/sketch
    :host "host"
    :size [WIDTH HEIGHT]
    :draw #(draw @state)
    :on-close (fn [] (swap! state #(assoc % :stop true))
  )))

(defn start [n]
  (let [state (atom (init-world n))]
    (async/thread (run-sim state))
    (start-sketch state)))

; ------------ Syncronous --------------
(defn setup []
  (q/frame-rate 30))

(defn start-sketch-sync [state]
  (q/sketch
    :host "host"
    :size [WIDTH HEIGHT]
    :setup (fn [] (setup) state)
    :draw #(draw %)
    :update #(update-particles %)
;    :on-close (fn [_] (q/save-frame "frame###.png")) 
    :middleware [m/fun-mode]
  ))

(defn start-sync [n]
  (let [state (init-particles n WIDTH HEIGHT)]
    (start-sketch-sync state)))
(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))
