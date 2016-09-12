(ns net.kiertscher.draw.pencil-test
  (:require [clojure.test :refer [deftest]]
            [net.kiertscher.draw.pencil :as p]
    #?(:clj
            [net.kiertscher.draw.pencil.jvm-awt :as awt]
       :cljs [net.kiertscher.draw.pencil.js-canvas :as jsc]))
  #?(:clj
     (:import [java.io File])))

(defn sketch-draw-line [ctx]
  (-> ctx
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

(def test-sketches
  {:draw-line {:f sketch-draw-line :w 200 :h 50}})

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
