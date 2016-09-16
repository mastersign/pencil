(ns net.kiertscher.draw.pencil.js-canvas
  (:require [net.kiertscher.draw.pencil :as core]))

(defn info
  "Display an info message."
  [txt]
  (js/alert txt))

(defn- format-color-value
  [v]
  (.toString
    (int (.floor js/Math (* 255.0 (min 1.0 (max 0.0 v)))))))

(defn- format-alpha-value
  [v]
  (.toString
    (/ (.floor js/Math (* 255.0 (min 1.0 (max 0.0 v)))) 255.0)))

(defn- color->css
  [c]
  (str "rgba("
       (format-color-value (:r c)) ","
       (format-color-value (:g c)) ","
       (format-color-value (:b c)) ","
       (format-alpha-value (:a c))
       ")"))

(defn- name
  [v]
  (.slice (str v) 1))

(defrecord HtmlCanvasContext
  [id g]

  core/IClearing

  (clear-rect [ctx x y w h]
    (.clearRect g x y w h)
    ctx)

  (clear-all [ctx]
    (.clear g)
    ctx)

  core/IDrawing

  (set-line-style [ctx s]
    (let [{:keys [width color line-cap line-join]} (core/make-up-line-style s)]
      (set! (.-strokeStyle g) (color->css color))
      (set! (.-lineWidth g) width)
      (set! (.-lineCap g) (name line-cap))
      (set! (.-lineJoin g) (name line-join)))
    ctx)

  (draw-line [ctx x1 y1 x2 y2]
    (doto g
      (.beginPath)
      (.moveTo x1 y1)
      (.lineTo x2 y2)
      (.stroke))
    ctx)

  (draw-rect [ctx x y w h]
    (doto g
      (.beginPath)
      (.rect x y w h)
      (.stroke))
    ctx)

  (draw-arc [ctx x y r]
    (doto g
      (.beginPath)
      (.moveTo (+ x r) y)
      (.arc x y r 0 (* 2 Math/PI))
      (.closePath)
      (.stroke))
    ctx)

  (draw-arc [ctx x y r start extend]
    (doto g
      (.beginPath)
      (.arc x y r
            (if (pos? extend) start (+ start extend))
            (if (pos? extend) (+ start extend) start)))
    (when (>= (Math/abs extend) (* 2 Math/PI))
      (.closePath g))
    (.stroke g)
    ctx)

  core/IFilling

  (set-fill-style [ctx s]
    (let [{:keys [color]} (core/make-up-fill-style s)]
      (set! (.-fillStyle g) (color->css color)))
    ctx)

  (fill-rect [ctx x y w h]
    (.fillRect g x y w h)
    ctx))

(defn draw
  [id f]
  (let [el (.getElementById js/document id)
        ctx (.getContext el "2d")]
    (-> (->HtmlCanvasContext id ctx)
        (core/set-line-style (core/line-style))
        (core/set-fill-style (core/fill-style))
        (f))))
