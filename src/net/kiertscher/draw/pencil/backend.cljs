(ns net.kiertscher.draw.pencil.backend
  (:require [net.kiertscher.draw.pencil.core :refer [IGraphicsContext]]))

(defn info
  "Display an info message."
  [txt]
  (js/alert txt))

(defrecord CanvasContext
  [id ctx]

  IGraphicsContext

  (draw-rect [_ x y w h]
    (.rect ctx x y w h)
    (.stroke ctx))

  (fill-rect [_ x y w h]
    (.rect ctx x y w h)
    (.fill ctx)))

(defn open-canvas-context
  [id]
  (let [el (.getElementById js/document id)
        ctx (.getContext el "2d")]
    (set! (.-lineWidth ctx) 1.0)
    (set! (.-strokeStyle ctx) "blue")
    (set! (.-fillStyle ctx) "red")
    (->CanvasContext id ctx)))

(defn draw-on-canvas
  [id f]
  (let [ctx (open-canvas-context id)]
    (f ctx)))
