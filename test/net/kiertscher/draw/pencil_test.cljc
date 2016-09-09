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
      (p/draw-line 30 10 35 40)))

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
