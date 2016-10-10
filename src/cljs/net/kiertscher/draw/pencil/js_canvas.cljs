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
  [id width height g]

  core/IClearing

  (clear-rect [ctx x y w h]
    (.clearRect g x y w h)
    ctx)

  (clear-all [ctx]
    (.clearRect g 0 0 width height)
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
      (.arc x y r start (+ start extend) (neg? extend)))
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
      (.ellipse x y rx ry 0 start (+ start extend) (neg? extend)))
    (when (>= (Math/abs extend) (* 2 Math/PI))
      (.closePath g))
    (.stroke g))

  (draw-quadratic-curve [_ x1 y1 cx cy x2 y2]
    (doto g
      (.beginPath)
      (.moveTo x1 y1)
      (.quadraticCurveTo cx cy x2 y2)
      (.stroke)))

  (draw-cubic-curve [_ x1 y1 cx1 cy1 cx2 cy2 x2 y2]
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
      (.arc x y r start (+ start extend) (neg? extend))
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
      (.ellipse x y rx ry 0 start (+ start extend) (neg? extend))
      (.closePath)
      (.fill)))

  core/IPathRendering

  (begin-path [_]
    (.beginPath g))

  (path-close [_]
    (.closePath g))

  (path-move-to [_ x y]
    (.moveTo g x y))

  (path-line-to [_ x y]
    (.lineTo g x y))

  (path-quadratic-curve-to [_ cx cy x y]
    (.quadraticCurveTo g cx cy x y))

  (path-cubic-curve-to [_ cx1 cy1 cx2 cy2 x y]
    (.bezierCurveTo g cx1 cy1 cx2 cy2 x y))

  (path-arc [_ x y r]
    (doto g
      (.moveTo (+ x r) y)
      (.arc  x y r 0 (* 2 Math/PI))
      (.closePath)))

  (path-arc [_ x y r start extend]
    (if (>= (Math/abs extend) (* 2 Math/PI))
      (doto g
        (.moveTo (+ x r) y)
        (.arc x y r 0 (* 2 Math/PI))
        (.closePath))
      (.arc g x y r
            start
            (+ start extend)
            (neg? extend))))

  (path-ellipse [_ x y rx ry]
    (doto g
      (.moveTo (+ x rx) y)
      (.ellipse x y rx ry 0 0 (* 2 Math/PI))
      (.closePath)))

  (path-ellipse [_ x y rx ry start extend]
    (if (>= (Math/abs extend) (* 2 Math/PI))
      (doto g
        (.moveTo (+ x rx) y)
        (.ellipse x y rx ry 0 (* 2 Math/PI))
        (.closePath))
      (.ellipse g x y rx ry 0
                start
                (+ start extend)
                (neg? extend))))

  (draw-path [_]
    (.stroke g))

  (fill-path [_]
    (.fill g))

  (fill-path [_ rule]
    (.fill g (if (= rule :even-odd) "evenodd" "nonzero")))

  (clip-path [_]
    (.clip g))

  (clip-path [_ rule]
    (.clip g (if (= rule :even-odd) "evenodd" "nonzero")))

  core/ITransforming

  (set-transform [_ a b c d e f]
    (.setTransform g a b c d e f))

  (transform [_ a b c d e f]
    (.transform g a b c d e f))

  (translate [_ x y]
    (.translate g x y))

  (scale [_ x y]
    (.scale g x y))

  (rotate [_ a]
    (.rotate g a))

  core/IClipping

  (clip-rect [_ x y w h]
    (doto g
      (.beginPath)
      (.rect x y w h)
      (.clip)))

  core/IStashing

  (push-state [_]
    (.save g))

  (pop-state [_]
    (.restore g)))

(defn render
  [id f]
  (let [el (.getElementById js/document id)
        ctx (.getContext el "2d")]
    (doto (->HtmlCanvasContext id (.-width el) (.-height el) ctx)
      (core/set-line-style (core/line-style))
      (core/set-fill-style (core/fill-style))
      (f))))
