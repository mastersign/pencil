(ns net.kiertscher.draw.pencil.core)

(defrecord Color
  [r g b a])

(defn color
  "Creates an RGBA color value, with every component between 0 and 1."
  ([r g b a]
   (->Color r g b a))
  ([r g b]
   (->Color r g b 1.0))
  ([v a]
   (->Color v v v a))
  ([v]
   (->Color v v v 1.0)))

(def ^:dynamic *default-line-color* (color 0.25))
(def ^:dynamic *default-line-width* 1.0)
(def ^:dynamic *default-fill-color* (color 0.75))

(defrecord LineStyle
  [^float width
   ^Color color])

(defn line-style
  ([^double width
    ^Color color]
   (->LineStyle width color))
  ([^Color color]
   (->LineStyle *default-line-width* color))
  ([]
   (->LineStyle *default-line-width* *default-line-color*)))

(defn complete-line-style
  [{:keys [width color]
    :or   {:width *default-line-width*
           :color *default-line-color*}
    :as   style}]
  (if (instance? LineStyle style)
    style
    (->LineStyle width color)))

(defrecord FillStyle
  [^Color color])

(defn fill-style
  ([^Color color]
   (->FillStyle color))
  ([]
   (->FillStyle *default-fill-color*)))

(defn complete-fill-style
  [{:keys [color]
    :or {:color *default-fill-color*}
    :as style}]
  (if (instance? FillStyle style)
    style
    (->FillStyle color)))

(defprotocol IClearing

  (clear-rect [_ x y w h]
    "Resets a rectangle to transparent black.")

  (clear-all [_]
    "Resets the entire drawing to transparent black."))

(defprotocol IDrawing

  (set-line-style [_ s]
    "Set the style for line drawing.")

  (draw-line [_ x1 y1 x2 y2]
    "Draw a line from one point to another.")

  (draw-rect [_ x y w h]
    "Draw an axis-aligned rectangle.")

  (draw-circle [_ x y r]
    "Draw a circle around a center point.")

  (draw-ellipis [_ x y w h]
    "Draw an ellipsis in a bounding box."))

(defprotocol IFilling

  (set-fill-style [_ s]
    "Set the style for filling shapes.")

  (fill-rect [_ x y w h]
    "Fill an axis-aligned rectangle.")

  (fill-circle [_ x y r]
    "Fill a circle around a center point")

  (fill-ellipsis [_ x y w h]
    "Fill an ellipsis in a bounding box."))