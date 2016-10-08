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

  (set-line-style [_ s]
    (let [{:keys [width color line-cap line-join]} (core/make-up-line-style s)]
      (set! (.-strokeStyle g) (color->css color))
      (set! (.-lineWidth g) width)
      (set! (.-lineCap g) (name line-cap))
      (set! (.-lineJoin g) (name line-join))))

  (draw-line [_ x1 y1 x2 y2]
    (doto g
      (.beginPath)
      (.moveTo x1 y1)
      (.lineTo x2 y2)
      (.stroke)))

  (draw-rect [_ x y w h]
    (doto g
      (.beginPath)
      (.rect x y w h)
      (.stroke)))

  (draw-arc [_ x y r]
    (doto g
      (.beginPath)
      (.moveTo (+ x r) y)
      (.arc x y r 0 (* 2 Math/PI))
      (.closePath)
      (.stroke)))

  (draw-arc [_ x y r start extend]
    (doto g
      (.beginPath)
      (.arc x y r
            (if (pos? extend) start (+ start extend))
            (if (pos? extend) (+ start extend) start)))
    (when (>= (Math/abs extend) (* 2 Math/PI))
      (.closePath g))
    (.stroke g))

  (draw-ellipse [_ x y rx ry]
    (doto g
      (.beginPath)
      (.ellipse x y rx ry 0 0 (* 2 Math/PI))
      (.closePath)
      (.stroke)))

  (draw-ellipse [_ x y rx ry start extend]
    (doto g
      (.beginPath)
      (.ellipse x y rx ry 0
                (if (pos? extend) start (+ start extend))
                (if (pos? extend) (+ start extend) start)))
    (when (>= (Math/abs extend) (* 2 Math/PI))
      (.closePath g))
    (.stroke g))

  (draw-quadratic-bezier [_ x1 y1 cx cy x2 y2]
    (doto g
      (.beginPath)
      (.moveTo x1 y1)
      (.quadraticCurveTo cx cy x2 y2)
      (.stroke)))

  (draw-cubic-bezier [_ x1 y1 cx1 cy1 cx2 cy2 x2 y2]
    (doto g
      (.beginPath)
      (.moveTo x1 y1)
      (.bezierCurveTo cx1 cy1 cx2 cy2 x2 y2)
      (.stroke)))

  core/IFilling

  (set-fill-style [_ s]
    (let [{:keys [color]} (core/make-up-fill-style s)]
      (set! (.-fillStyle g) (color->css color))))

  (fill-rect [_ x y w h]
    (doto g
      (.beginPath)
      (.rect x y w h)
      (.fill)))

  (fill-arc [_ x y r]
    (doto g
      (.beginPath)
      (.arc x y r 0 (* 2 Math/PI))
      (.closePath)
      (.fill)))

  (fill-arc [_ x y r start extend]
    (.beginPath g)
    (when (< (Math/abs extend) (* 2 Math/PI))
      (.moveTo g x y))
    (doto g
      (.arc x y r
            (if (pos? extend) start (+ start extend))
            (if (pos? extend) (+ start extend) start))
      (.closePath)
      (.fill)))

  (fill-ellipse [_ x y rx ry]
    (doto g
      (.beginPath)
      (.ellipse x y rx ry 0 0 (* 2 Math/PI))
      (.closePath)
      (.fill)))

  (fill-ellipse [_ x y rx ry start extend]
    (.beginPath g)
    (when (< (Math/abs extend) (* 2 Math/PI))
      (.moveTo g x y))
    (doto g
      (.ellipse x y rx ry 0
                (if (pos? extend) start (+ start extend))
                (if (pos? extend) (+ start extend) start))
      (.closePath)
      (.fill))))

(defn draw
  [id f]
  (let [el (.getElementById js/document id)
        ctx (.getContext el "2d")]
    (doto (->HtmlCanvasContext id ctx)
        (core/set-line-style (core/line-style))
        (core/set-fill-style (core/fill-style))
        (f))))
