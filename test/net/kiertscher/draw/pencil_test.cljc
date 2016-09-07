(ns net.kiertscher.draw.pencil-test
  (:require [clojure.test :refer [deftest]]
            [net.kiertscher.draw.pencil :as p]
    #?(:clj
            [net.kiertscher.draw.pencil.jvm-awt :as awt]
       :cljs [net.kiertscher.draw.pencil.js-canvas :as jsc]))
  #?(:clj
     (:import [java.io File])))

(defn drawing-basics [ctx]
  (p/set-line-style ctx (p/line-style (p/color 1.0 0.0 0.5) 2.0))
  (p/set-fill-style ctx (p/fill-style (p/color 0.5 0.5)))
  (p/fill-rect ctx 10 10 180 180)
  (p/set-fill-style ctx (p/fill-style (p/color 0.5)))
  (p/fill-rect ctx 50 50 100 100)
  (doseq [v (range 20 60 5)]
    (p/draw-rect ctx v v v v))
  (p/clear-rect ctx 100 100 50 50))

(def test-drawings
  {:basics {:f drawing-basics :w 200 :h 200}})

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

(doseq [[id {:keys [w h f]}] test-drawings]
  (draw (name id) w h f))
