(ns net.kiertscher.draw.js-canvas-test
  (:require [net.kiertscher.draw.pencil-test :as t]
            [net.kiertscher.draw.pencil.js-canvas :as jsc]))

(defn- draw
  [id w h f]
  (let [e (.getElementById js/document id)]
    (set! (.-width e) w)
    (set! (.-height e) h))
  (jsc/draw id f))

(defn- get-el
  [id]
  (.getElementById js/document id))

(def ^:private diff-factor 8)

(defn- pixel-difference
  [vs1 vs2 vsr i]
  (let [ir i
        ig (+ i 1)
        ib (+ i 2)
        ia (+ i 3)
        rd (.abs js/Math (- (aget vs1 ir) (aget vs2 ir)))
        gd (.abs js/Math (- (aget vs1 ig) (aget vs2 ig)))
        bd (.abs js/Math (- (aget vs1 ib) (aget vs2 ib)))
        ad (.abs js/Math (- (aget vs1 ia) (aget vs2 ia)))]
    (aset vsr ir (- 255 (* (+ gd bd ad) diff-factor)))
    (aset vsr ig (- 255 (* (+ rd bd ad) diff-factor)))
    (aset vsr ib (- 255 (* (+ rd gd ad) diff-factor)))
    (aset vsr ia 255)
    [rd gd bd ad]))

(defn- image-data-difference
  [img-data-1 img-data-2]
  (let [w (.-width img-data-1)
        h (.-height img-data-1)
        a1 (.-data img-data-1)
        a2 (.-data img-data-2)]
    (doseq [y (range h)
            x (range w)]
      (pixel-difference a1 a2 a2
                        (* 4 (+ x (* y w)))))))

(doseq [[id {:keys [w h f]}] t/test-sketches]
  (draw (name id) w h f)
  (let [n (name id)
        can (get-el n)
        img-awt (get-el (str n "-awt"))
        can-diff (get-el (str n "-diff"))
        ctx (.getContext can "2d")
        ctx-diff (.getContext can-diff "2d")]
    (set! (.-width can-diff) w)
    (set! (.-height can-diff) h)
    (.drawImage ctx-diff img-awt 0 0)
    (let [data (.getImageData ctx 0 0 w h)
          data-diff (.getImageData ctx-diff 0 0 w h)]
      (image-data-difference data data-diff)
      (.putImageData ctx-diff data-diff 0 0))))
