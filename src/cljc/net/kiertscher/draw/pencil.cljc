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
    "Set the style for line drawing.")

  (draw-line [_ x1 y1 x2 y2]
    "Draw a line from one point to another.")

  (draw-rect [_ x y w h]
    "Draw an axis-aligned rectangle.")

  (draw-arc [_ x y r] [_ x y r start extend]
    "Draw an arc around a center point.")

  (draw-ellipse [_ x y rx ry] [_ x y rx ry start extend]
    "Draw an axis-aligned ellipse around a center point.")

  (draw-quadratic-curve [_ x1 y1 cx cy x2 y2]
    "Draws a quadratic Bézier curve.")

  (draw-cubic-curve [_ x1 y1 cx1 cy1 cx2 cy2 x2 y2]
    "Draws a cubic Bézier curve."))

(defn- catmull-rom [t cx1 cy1 x1 y1 x2 y2 cx2 cy2]
  [x1 y1
   (/ (+ (- cx1) (* t x1) x2) t) (/ (+ (- cy1) (* t y1) y2) t)
   (/ (+ x1 (* t x2) (- cx2)) t) (/ (+ y1 (* t y2) (- cy2)) t)
   x2 y2])

(defn draw-catmull-rom
  "Draws a Catmull-Rom spline."
  ([ctx cx1 cy1 x1 y1 x2 y2 cx2 cy2]
   (apply draw-cubic-curve ctx
          (catmull-rom 6.0 cx1 cy1 x1 y1 x2 y2 cx2 cy2)))
  ([ctx t cx1 cy1 x1 y1 x2 y2 cx2 cy2]
   (apply draw-cubic-curve ctx
          (catmull-rom t cx1 cy1 x1 y1 x2 y2 cx2 cy2))))

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
    "Set the style for filling shapes.")

  (fill-rect [_ x y w h]
    "Fill an axis-aligned rectangle.")

  (fill-arc [_ x y r] [_ x y r start extend]
    "Fill an arc around a center point.")

  (fill-ellipse [_ x y rx ry] [_ x y rx ry start extend]
    "Fill an axis-aligned ellipsis around a center point."))

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

(defprotocol IPathRendering

  (begin-path [_]
    "Begins a new path.")

  (path-close [_]
    "Closes the current path to a loop by connecting the last position
     and the beginning of the path with a straight line.")

  (path-move-to [_ x y]
    "Moves the last position in the current path to the given location.")

  (path-line-to [_ x y]
    "Adds a straight line to the current path, connecting the last position
    and the given location.")

  (path-quadratic-curve-to [_ cx cy x y]
    "Adds a quadratic Bézier curve to the current path.
     The beginning of the curve is the last position.")

  (path-cubic-curve-to [_ cx1 cy1 cx2 cy2 x y]
    "Adds a cubic Bézier curve to the current path.
     The beginning of the curve is the last position.")

  (path-arc [_ x y r] [_ x y r start extend]
    "Adds an arc around a center point to the current path.
    If the current path is not empty, the last position is connected
    to the beginning of the arc by a straight line.")

  (path-ellipse [_ x y rx ry] [_ x y rx ry start extend]
    "Adds an axis-aligned ellipse around a center point to the current path.
     If the current path is not empty, the last position is connected
     to the beginning of the ellipse by a straight line.")

  (draw-path [_]
    "Draws the current path.")

  (fill-path [_] [_ rule]
    "Fills the current path.
     The rule can be :non-zero or :even-odd.
     The default rule is :non-zero.")

  (clip-path [_] [_ rule]
    "Constraints the clipping region with the current path.
     The rule can be :non-zero or :even-odd.
     The default rule is :non-zero."))

(defprotocol ITransforming

  (set-transform [_ a b c d e f]
    "Sets the transformation matrix to:
     | a c e |
     | b d f |
     | 0 0 1 |")

  (transform [_ a b c d e f]
    "Multiplies the current transformation matrix with:
     | a c e |
     | b d f |
     | 0 0 1 |")

  (translate [_ x y]
    "Adds a translation to the current transformation.")

  (scale [_ x y]
    "Adds a scaling to the current transformation.")

  (rotate [_ a]
    "Adds a rotation in radians to the current transformation."))

(defn reset-transform [ctx]
  "Resets the transformation matrix to the identiy matrix."
  (set-transform ctx 1 0 0 1 0 0))

(defprotocol IClipping

  (clip-rect [_ x y w h]
    "Constraints further drawing to the inner of the given axis-align rectangle."))

(defprotocol IStashing

  (push-state [_]
    "Pushes the current context state onto the stack.
     The state contains draw and fill style, transformation and clipping.")

  (pop-state [_]
    "Pops the last context state from the stack.
     The state contains draw and fill style, transformation and clipping."))
