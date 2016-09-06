(ns net.kiertscher.draw.pencil.backend
  (:require [net.kiertscher.draw.pencil.core :as core]))

(defn info
  "Display an info message."
  [txt]
  (js/alert txt))

(defn- format-color-value
  [v]
  (.toString
    (int (* 255.0 (min 1.0 (max 0.0 v))))))

(defn- color->css
  [c]
  (str "rgba("
       (format-color-value (:r c)) ","
       (format-color-value (:g c)) ","
       (format-color-value (:b c)) ","
       (.toString (:a c))
       ")"))

(defrecord CanvasContext
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
    (let [{:keys [width color]} (core/complete-line-style s)]
      (set! (.-strokeStyle g) (color->css color))
      (set! (.-lineWidth g) width))
    ctx)

  (draw-rect [ctx x y w h]
    (.strokeRect g x y w h)
    ctx)

  core/IFilling

  (set-fill-style [ctx s]
    (let [{:keys [color]} (core/complete-fill-style s)]
      (set! (.-fillStyle g) (color->css color)))
    ctx)

  (fill-rect [ctx x y w h]
    (.fillRect g x y w h)
    ctx))

(defn open-canvas-context
  [id]
  (let [el (.getElementById js/document id)
        ctx (.getContext el "2d")]
    (-> (->CanvasContext id ctx)
        (core/set-line-style (core/line-style))
        (core/set-fill-style (core/fill-style)))))

(defn draw-on-canvas
  [id f]
  (let [ctx (open-canvas-context id)]
    (f ctx)))
