(ns net.kiertscher.draw.pencil-test
  (:require [clojure.test :refer [deftest]]
            [net.kiertscher.draw.pencil :as p]
    #?(:clj
            [net.kiertscher.draw.pencil.jvm-awt :as awt]
       :cljs [net.kiertscher.draw.pencil.js-canvas :as jsc]))
  #?(:clj
     (:import [java.io File])))

(defn sketch-draw-line [ctx]
  (doto ctx
    (p/set-line-style (p/line-style))
    (p/draw-line 10 10 10 40)
    (p/draw-line 15.333 10 15.333 40)
    (p/draw-line 20.5 10 20.5 40)
    (p/draw-line 25 10 26 40)
    (p/draw-line 30 10 35 40)
    (p/draw-line 40 15 45 35)
    (p/draw-line 50 20 55 30)
    (p/draw-line 60 22 66 28)

    (p/draw-line 90 10 130 10)
    (p/draw-line 90 15.2 130 15.3)
    (p/draw-line 90 20.4 130 20.6)
    (p/draw-line 90 25.6 130 25.9)
    (p/draw-line 90 30.8 130 31.2)
    (p/draw-line 90 31.0 130 31.5)
    (p/draw-line 90 36.2 130 36.8)
    (p/draw-line 90 41.4 130 42.1))

  (doseq [a (range 15)]
    (let [x (* 20 (Math/cos (* (/ Math/PI 15) a)))
          y (* 20 (Math/sin (* (/ Math/PI 15) a)))]
      (p/draw-line ctx (+ 170 x) (+ 25 y) (- 170 x) (- 25 y)))))

(defn sketch-draw-rect [ctx]
  (doto ctx
    (p/set-line-style (p/line-style))
    (p/draw-rect 10 10 30 30)
    (p/draw-rect 15.5 15 20 20)
    (p/draw-rect 20 20.5 10 10)
    (p/draw-rect 22.5 22.5 4 4)
    (p/draw-rect 50 10 0 30)
    (p/draw-rect 55 10 0.5 30)
    (p/draw-rect 60 10 1 30)
    (p/draw-rect 65 10 1.5 30)
    (p/draw-rect 70 10 2 30)
    (p/draw-rect 80.2 10 2 30)
    (p/draw-rect 85.4 10 2 30)
    (p/draw-rect 90.6 10 2 30)
    (p/draw-rect 95.8 10 2 30)
    (p/draw-rect 100 10 2 30)
    (p/draw-rect 120 14 20 0)
    (p/draw-rect 120 22.5 20 2)
    (p/draw-rect 120.5 32 20 3)))

(defn sketch-line-style [ctx]
  (let [pattern (fn [ctx x y]
                  (doto ctx
                    (p/draw-line x y x (+ y 30))
                    (p/draw-line (+ x 10.5) (+ y 30) (+ x 10.5) y)
                    (p/draw-line (+ x 20) y (+ x 30) y)
                    (p/draw-rect (+ x 20) (+ y 8.5) 10.5 10.5)
                    (p/draw-line (+ x 20) (+ y 25) (+ x 30) (+ y 30))
                    (p/draw-line (+ x 20) (+ y 30) (+ x 30) (+ y 25))))]
    (doto ctx
      (p/set-line-style (p/line-style (p/color 0) 1.0 :square))
      (pattern 10 10)
      (p/set-line-style (p/line-style (p/color 0) 1.5 :square))
      (pattern 60 10)
      (p/set-line-style (p/line-style (p/color 0) 3.333 :square))
      (pattern 110 10)
      (p/set-line-style (p/line-style (p/color 0) 3.9 :square))
      (pattern 160 10)
      (p/set-line-style (p/line-style (p/color 0) 5.0 :butt :bevel))
      (pattern 10 60)
      (p/set-line-style (p/line-style (p/color 0) 5.0 :square :miter))
      (pattern 60 60)
      (p/set-line-style (p/line-style (p/color 0) 5.0 :round :round))
      (pattern 110 60))))

(defn sketch-draw-arc [ctx]
  (doto ctx
    (p/draw-arc 25 25 15)
    (p/draw-arc 25.5 25 10)
    (p/draw-arc 25 25.5 5)
    (p/draw-arc 70 25 15 (* 0 Math/PI) (* 2.0 Math/PI))
    (p/draw-arc 70 25 12.5 (* 0 Math/PI) (* 1.0 Math/PI))
    (p/draw-arc 70 25 10 (* 0 Math/PI) (* -1.0 Math/PI))
    (p/draw-arc 70 25 7.5 (* 1 Math/PI) (* 0.5 Math/PI))
    (p/draw-arc 70 25 5 (* 1 Math/PI) (* -0.5 Math/PI))))

(def test-sketches
  {:draw-line  {:f sketch-draw-line :w 200 :h 50}
   :draw-rect  {:f sketch-draw-rect :w 200 :h 50}
   :line-style {:f sketch-line-style :w 200 :h 100}
   :draw-arc {:f sketch-draw-arc :w 200 :h 50}})

(defn draw
  [id w h f]
  #?(:clj  (do
             (.mkdirs (File. "out/test/awt"))
             (awt/draw-in-file
               (str "out/test/awt/" id ".png") "PNG"
               w h f))
     :cljs (do
             (let [e (.getElementById js/document id)]
               (set! (.-width e) w)
               (set! (.-height e) h))
             (jsc/draw id f))))

(doseq [[id {:keys [w h f]}] test-sketches]
  (draw (name id) w h f))
