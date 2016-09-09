(ns net.kiertscher.draw.pencil.js-canvas-test
  (:require [net.kiertscher.draw.pencil-test :refer [test-sketches]]))

(defn- get-el
  [id]
  (.getElementById js/document id))

(defn- pixel-difference
  [a1 a2 ar i]
  (let [ir i
        ig (+ i 1)
        ib (+ i 2)
        ia (+ i 3)
        color-diff (not (and (= (aget a1 ir) (aget a2 ir))
                             (= (aget a1 ig) (aget a2 ig))
                             (= (aget a1 ib) (aget a2 ib))))
        alpha-diff (not (= (aget a1 ia) (aget a2 ia)))]
    (aset ar ir (if color-diff 220 (if alpha-diff 255 128)))
    (aset ar ig (if color-diff 0 (if alpha-diff 128 255)))
    (aset ar ib (if color-diff 0 (if alpha-diff 0 128)))
    (aset ar ia 255)))

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

(do
  (doseq [[id {:keys [w h]}] test-sketches]
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
        (.putImageData ctx-diff data-diff 0 0)))))
