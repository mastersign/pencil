(ns net.kiertscher.draw.pencil)

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
(def ^:dynamic *default-line-cap* :square)
(def ^:dynamic *default-line-join* :miter)
(def ^:dynamic *default-fill-color* (color 0.75))

(defprotocol IClearing

  (clear-rect [_ x y w h]
    "Resets a rectangle to transparent black.")

  (clear-all [_]
    "Resets the entire drawing to transparent black."))

(defprotocol IDrawing

  (set-line-style [_ s]
    "Set the style for line drawing.
     Returns the context object.")

  (draw-line [_ x1 y1 x2 y2]
    "Draw a line from one point to another.
     Returns the context object.")

  (draw-rect [_ x y w h]
    "Draw an axis-aligned rectangle.
     Returns the context object.")

  (draw-arc [_ x y r] [_ x y r start extend]
    "Draw an arc around a center point.
     Returns the context object.")
  
  (draw-ellipse [_ x y rx ry] [_ x y rx ry start extend]
    "Draw an ellipse around a center point.
     Returns the context object."))

(defrecord LineStyle
  [^Color color
   ^float width
   ;; :butt, :round, :square
   line-cap
   ;; :bevel, :round, :miter
   line-join])

(defn line-style
  ([^Color color
    ^double width
    line-cap
    line-join]
   (->LineStyle color
                width
                line-cap
                line-join))
  ([^Color color
    ^double width
    line-cap]
   (->LineStyle color
                width
                line-cap
                *default-line-join*))
  ([^Color color
    ^double width]
   (->LineStyle color
                width
                *default-line-cap*
                *default-line-join*))
  ([^Color color]
   (->LineStyle color
                *default-line-width*
                *default-line-cap*
                *default-line-join*))
  ([]
   (->LineStyle *default-line-color*
                *default-line-width*
                *default-line-cap*
                *default-line-join*)))

(defn make-up-line-style
  [style]
  (if (instance? LineStyle style)
    style
    (let [{:keys [color width line-cap line-join]
           :or   {:color     *default-line-color*
                  :width     *default-line-width*
                  :line-cap  *default-line-cap*
                  :line-join *default-line-join*}} style]
      (->LineStyle color width line-cap line-join))))

(defprotocol IFilling

  (set-fill-style [_ s]
    "Set the style for filling shapes.
     Returns the context object.")

  (fill-rect [_ x y w h]
    "Fill an axis-aligned rectangle.
     Returns the context object.")

  (fill-circle [_ x y r]
    "Fill a circle around a center point.
     Returns the context object.")

  (fill-ellipsis [_ x y w h]
    "Fill an ellipsis in a bounding box.
     Returns the context object."))

(defrecord FillStyle
  [^Color color])

(defn fill-style
  ([^Color color]
   (->FillStyle color))
  ([]
   (->FillStyle *default-fill-color*)))

(defn make-up-fill-style
  [{:keys [color]
    :or   {:color *default-fill-color*}
    :as   style}]
  (if (instance? FillStyle style)
    style
    (->FillStyle color)))