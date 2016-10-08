(ns net.kiertscher.draw.pencil-test
  (:require [net.kiertscher.draw.pencil :as p]))

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
      (p/set-line-style (p/line-style (p/color 0.0) 1.0 :square))
      (pattern 10 10)
      (p/set-line-style (p/line-style (p/color 0.0 0.3) 1.5 :square))
      (pattern 60 10)
      (p/set-line-style (p/line-style (p/color 0.5 0.5 0.5) 3.333 :square))
      (pattern 110 10)
      (p/set-line-style (p/line-style (p/color 0.2 0.5 0.8) 3.9 :square))
      (pattern 160 10)
      (p/set-line-style (p/line-style (p/color 0.5 0.1 0.1 0.3) 5.0 :butt :bevel))
      (pattern 10 60)
      (p/set-line-style (p/line-style (p/color 0.1 0.5 0.1 0.5) 5.0 :square :miter))
      (pattern 60 60)
      (p/set-line-style (p/line-style (p/color 0.1 0.1 0.5 0.8) 5.0 :round :round))
      (pattern 110 60))))

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

(def test-rects
  [[10 10 30 30]
   [15.5 15 20 20]
   [20 20.5 10 10]
   [22.5 22.5 4 4]
   [50 10 0 30]
   [55 10 0.5 30]
   [60 10 1 30]
   [65 10 1.5 30]
   [70 10 2 30]
   [80.2 10 2 30]
   [85.4 10 2 30]
   [90.6 10 2 30]
   [95.8 10 2 30]
   [100 10 2 30]
   [120 14 20 0]
   [120 22.5 20 2]
   [120.5 32 20 3]])

(defn sketch-draw-rect [ctx]
  (p/set-line-style ctx (p/line-style))
  (doseq [r test-rects]
    (apply p/draw-rect ctx r)))

(defn sketch-fill-rect [ctx]
  (p/set-fill-style ctx (p/fill-style (p/color 0.0 0.25)))
  (doseq [r test-rects]
    (apply p/fill-rect ctx r)))

(def test-arcs
  [[25 25 15]
   [25.5 25 10]
   [25 25.5 5]
   [70 25 15 (* 0 Math/PI) (* 2.0 Math/PI)]
   [70 25 12.5 (* 0 Math/PI) (* 1.0 Math/PI)]
   [70 25 10 (* 0 Math/PI) (* -1.0 Math/PI)]
   [70 25 7.5 (* 1 Math/PI) (* 0.5 Math/PI)]
   [70 25 5 (* 1 Math/PI) (* -0.5 Math/PI)]])

(def test-ellipsis
  [[125 25 15 18]
   [125.5 25 12 10]
   [125 25.5 4 6]
   [170 25 18 15 (* 0 Math/PI) (* 2.0 Math/PI)]
   [170 25 10.5 12.5 (* 0 Math/PI) (* 1.0 Math/PI)]
   [170 25 8 10 (* 0 Math/PI) (* -1.0 Math/PI)]
   [170 25 3 7.5 (* 1 Math/PI) (* 0.5 Math/PI)]
   [170 25 5.5 6 (* 1 Math/PI) (* -0.5 Math/PI)]])

(defn sketch-draw-arc [ctx]
  (p/set-line-style ctx (p/line-style))
  (doseq [a test-arcs]
    (apply p/draw-arc ctx a))
  (doseq [e test-ellipsis]
    (apply p/draw-ellipse ctx e)))

(defn sketch-fill-arc [ctx]
  (p/set-fill-style ctx (p/fill-style (p/color 0.0 0.25)))
  (doseq [a test-arcs]
    (apply p/fill-arc ctx a))
  (doseq [e test-ellipsis]
    (apply p/fill-ellipse ctx e)))

(def test-quadratic-beziers
  [[10 10 80 20 40 40]
   [10 25 60 40 70 10]])

(def test-cubic-beziers
  [[90 10 90 50 130 50 130 10]
   [80 15 170 20 50 10 130 45]])

(def test-catmull-roms
  [[3.0 120 10 160 10 150 40 190 30]
   [3.0 160 10 150 40 190 30 140 10]])

(defn sketch-draw-curve [ctx]
  (p/set-line-style ctx (p/line-style))
  (doseq [b test-quadratic-beziers]
    (apply p/draw-quadratic-bezier ctx b))
  (doseq [b test-cubic-beziers]
    (apply p/draw-cubic-bezier ctx b))
  (doseq [cr test-catmull-roms]
    (apply p/draw-catmull-rom ctx cr)))

(defn sketch-clear [ctx]
  (doto ctx
    (p/set-fill-style (p/fill-style (p/color 0.5 0.7 0.9 0.8)))
    (p/fill-rect 0 20 200 10)
    (p/fill-rect 20 0 160 50)
    (p/clear-all)
    (p/fill-rect 0 0 200 50)
    (p/clear-rect 10 10 180 30)
    (p/clear-rect 20 20 160 10)))

(defn sketch-transform [ctx]
  (let [pattern (fn [ctx]
                  (doto ctx
                    (p/set-line-style (p/line-style (p/color 0.5) 4))
                    (p/draw-rect -10 -10 20 20)
                    (p/set-line-style (p/line-style (p/color 0) 2))
                    (p/draw-line -15 0 15 0)
                    (p/draw-line 0 -15 0 15)
                    (p/clear-rect -5 -5 10 10)
                    (p/set-fill-style (p/fill-style (p/color 1 0 0)))
                    (p/fill-rect -8 -8 7 7)
                    (p/set-fill-style (p/fill-style (p/color 0 1 0)))
                    (p/fill-rect 1 -8 7 7)
                    (p/set-fill-style (p/fill-style (p/color 0 0 1)))
                    (p/fill-rect -8 1 7 7)
                    (p/set-fill-style (p/fill-style (p/color 1 1 0)))
                    (p/fill-rect 1 1 7 7)))]
    (doto ctx
      (p/set-transform 1 0 0 1 25 25)
      (pattern)
      (p/reset-transform)
      (p/translate 75 25)
      (p/scale -2 1.5)
      (pattern)
      (p/reset-transform)
      (p/transform 0.5 0 0 -0.5 125 25)
      (p/transform 2 0 0 -2 0 0)
      (p/rotate (* Math/PI 0.666))
      (pattern)
      (p/set-transform 1.2 0.1 -0.8 1.2 170 25)
      (pattern))))

(def test-sketches
  {:line-style {:f sketch-line-style :w 200 :h 100}
   :draw-line  {:f sketch-draw-line :w 200 :h 50}
   :draw-rect  {:f sketch-draw-rect :w 200 :h 50}
   :fill-rect  {:f sketch-fill-rect :w 200 :h 50}
   :draw-arc   {:f sketch-draw-arc :w 200 :h 50}
   :fill-arc   {:f sketch-fill-arc :w 200 :h 50}
   :draw-curve {:f sketch-draw-curve :w 200 :h 50}
   :clear      {:f sketch-clear :w 200 :h 50}
   :transform  {:f sketch-transform :w 200 :h 50}})
