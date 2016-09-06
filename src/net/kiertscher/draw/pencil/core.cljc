(ns net.kiertscher.draw.pencil.core)

(defprotocol IGraphicsContext

  (draw-rect [_ x y w h]
    "Draw an axis-aligned rectangle.")

  (fill-rect [_ x y w h]
    "Fill an axis-aligned rectangle."))
