(ns net.kiertscher.draw.pencil.jvm-awt
  (:require [net.kiertscher.draw.pencil :as core])
  (:import [java.io File]
           [java.awt Graphics2D Color BasicStroke AlphaComposite RenderingHints]
           [java.awt.image BufferedImage]
           [java.awt.geom Line2D$Double
                          Rectangle2D$Double
                          Ellipse2D$Double
                          Arc2D$Double
                          QuadCurve2D$Double
                          CubicCurve2D$Double
                          AffineTransform]
           [javax.imageio ImageIO]
           [javax.swing JOptionPane]))

(defn info
  "Display an info message."
  [txt]
  (JOptionPane/showMessageDialog
    nil txt "Info Message" JOptionPane/INFORMATION_MESSAGE))

(defn- convert-color-value
  [v]
  (int (Math/floor (* 255.0 (min 1.0 (max 0.0 v))))))

(defn- convert-color
  [c]
  (Color. ^int (convert-color-value (:r c))
          ^int (convert-color-value (:g c))
          ^int (convert-color-value (:b c))
          ^int (convert-color-value (:a c))))

(def ^:private line-caps
  {:butt   BasicStroke/CAP_BUTT
   :round  BasicStroke/CAP_ROUND
   :square BasicStroke/CAP_SQUARE})

(defn- ^int convert-line-cap [v] (v line-caps))

(def ^:private line-joins
  {:round BasicStroke/JOIN_ROUND
   :bevel BasicStroke/JOIN_BEVEL
   :miter BasicStroke/JOIN_MITER})

(defn- convert-line-join [v] (v line-joins))

(defn- update-current-state
  [ctx k v]
  (swap! (:state ctx) (fn [s] (assoc-in s [:current k] v))))

(defn- get-current-state
  [ctx k]
  (get-in (deref (:state ctx)) [:current k]))

(defn- apply-current-line-style
  [ctx]
  (let [g (:g ctx)
        {:keys [color width line-cap line-join]} (get-current-state ctx :line-style)]
    (.setColor g (convert-color color))
    (.setStroke g (BasicStroke. width
                                (convert-line-cap line-cap)
                                (convert-line-join line-join)))))

(defn- apply-current-fill-style
  [ctx]
  (let [g (:g ctx)
        {:keys [color]} (get-current-state ctx :fill-style)]
    (.setColor g (convert-color color))))

(defn- clear-rect [g x y w h]
  (let [comp (.getComposite g)]
    (.setComposite g AlphaComposite/Clear)
    (.fillRect g x y w h)
    (.setComposite g comp)))

(defrecord JavaAwtContext
  [^BufferedImage img
   ^Graphics2D g
   state]

  core/IClearing

  (clear-rect [ctx x y w h]
    (clear-rect g x y w h)
    ctx)

  (clear-all [ctx]
    (clear-rect g 0 0 (.getWidth img) (.getHeight img))
    ctx)

  core/IDrawing

  (set-line-style [ctx s]
    (update-current-state ctx :line-style (core/make-up-line-style s))
    nil)

  (draw-line [ctx x1 y1 x2 y2]
    (apply-current-line-style ctx)
    (let [s (Line2D$Double. x1 y1 x2 y2)]
      (.draw g s)))

  (draw-rect [ctx x y w h]
    (apply-current-line-style ctx)
    (let [s (Rectangle2D$Double. x y w h)]
      (.draw g s)))

  (draw-arc [ctx x y r]
    (apply-current-line-style ctx)
    (let [s (Ellipse2D$Double. (- x r) (- y r) (* 2 r) (* 2 r))]
      (.draw g s)))

  (draw-arc [ctx x y r start extend]
    (apply-current-line-style ctx)
    (let [s (if (>= (Math/abs ^double extend) (* 2 Math/PI))
              (Ellipse2D$Double. (- x r) (- y r) (* 2 r) (* 2 r))
              (Arc2D$Double. (- x r) (- y r) (* 2 r) (* 2 r)
                             (* -180.0 (/ start Math/PI))
                             (* -180.0 (/ extend Math/PI))
                             Arc2D$Double/OPEN))]
      (.draw g s)))

  (draw-ellipse [ctx x y rx ry]
    (apply-current-line-style ctx)
    (let [s (Ellipse2D$Double. (- x rx) (- y ry) (* 2 rx) (* 2 ry))]
      (.draw g s)))

  (draw-ellipse [ctx x y rx ry start extend]
    (apply-current-line-style ctx)
    (let [s (if (>= (Math/abs ^double extend) (* 2 Math/PI))
              (Ellipse2D$Double. (- x rx) (- y ry) (* 2 rx) (* 2 ry))
              (Arc2D$Double. (- x rx) (- y ry) (* 2 rx) (* 2 ry)
                             (* -180.0 (/ start Math/PI))
                             (* -180.0 (/ extend Math/PI))
                             Arc2D$Double/OPEN))]
      (.draw g s)))

  (draw-quadratic-bezier [ctx x1 y1 cx cy x2 y2]
    (apply-current-line-style ctx)
    (let [s (QuadCurve2D$Double. x1 y1 cx cy x2 y2)]
      (.draw g s)))

  (draw-cubic-bezier [ctx x1 y1 cx1 cy1 cx2 cy2 x2 y2]
    (apply-current-line-style ctx)
    (let [s (CubicCurve2D$Double. x1 y1 cx1 cy1 cx2 cy2 x2 y2)]
      (.draw g s)))

  core/IFilling

  (set-fill-style [ctx s]
    (update-current-state ctx :fill-style (core/make-up-fill-style s))
    nil)

  (fill-rect [ctx x y w h]
    (apply-current-fill-style ctx)
    (let [s (Rectangle2D$Double. x y w h)]
      (.fill g s)))

  (fill-arc [ctx x y r]
    (apply-current-fill-style ctx)
    (let [s (Ellipse2D$Double. (- x r) (- y r) (* 2 r) (* 2 r))]
      (.fill g s)))

  (fill-arc [ctx x y r start extend]
    (apply-current-fill-style ctx)
    (let [s (if (>= (Math/abs ^double extend) (* 2 Math/PI))
              (Ellipse2D$Double. (- x r) (- y r) (* 2 r) (* 2 r))
              (Arc2D$Double. (- x r) (- y r) (* 2 r) (* 2 r)
                             (* -180.0 (/ start Math/PI))
                             (* -180.0 (/ extend Math/PI))
                             Arc2D$Double/PIE))]
      (.fill g s)))

  (fill-ellipse [ctx x y rx ry]
    (apply-current-fill-style ctx)
    (let [s (Ellipse2D$Double. (- x rx) (- y ry) (* 2 rx) (* 2 ry))]
      (.fill g s)))

  (fill-ellipse [ctx x y rx ry start extend]
    (apply-current-fill-style ctx)
    (let [s (if (>= (Math/abs ^double extend) (* 2 Math/PI))
              (Ellipse2D$Double. (- x rx) (- y ry) (* 2 rx) (* 2 ry))
              (Arc2D$Double. (- x rx) (- y ry) (* 2 rx) (* 2 ry)
                             (* -180.0 (/ start Math/PI))
                             (* -180.0 (/ extend Math/PI))
                             Arc2D$Double/PIE))]
      (.fill g s)))

  core/ITransforming

  (set-transform [ctx a b c d e f]
    (let [t (AffineTransform. ^double a ^double b
                              ^double c ^double d
                              ^double e ^double f)]
      (.setTransform g t)
      (update-current-state ctx :transform t)))

  (transform [ctx a b c d e f]
    (let [t (AffineTransform. ^double a ^double b
                              ^double c ^double d
                              ^double e ^double f)]
      (.transform g t))
    (update-current-state ctx :transform (.getTransform g)))

  (translate [ctx x y]
    (.translate g ^double x ^double y)
    (update-current-state ctx :transform (.getTransform g)))

  (scale [ctx x y]
    (.scale g ^double x ^double y)
    (update-current-state ctx :transform (.getTransform g)))

  (rotate [ctx a]
    (.rotate g ^double a)
    (update-current-state ctx :transform (.getTransform g)))

  core/IClipping

  (clip-rect [ctx x y w h]
    (let [s (Rectangle2D$Double. x y w h)]
      (.clip g s))
    (update-current-state ctx :clipping (.getClip g)))

  core/IStashing

  (push-state [_]
    (swap! state (fn [s] (assoc s :stack (conj (:stack s) (:current s))))))

  (pop-state [ctx]
    (swap! state (fn [s] (if (not (empty? (:stack s)))
                           (-> s
                               (assoc :current (last (:stack s)))
                               (assoc :stack (pop (:stack s))))
                           s)))
    (.setTransform g (get-current-state ctx :transform))
    (.setClip g (get-current-state ctx :clipping))))

(defn create-image
  [w h]
  (BufferedImage. w h BufferedImage/TYPE_INT_ARGB))

(defn save-image
  [^BufferedImage img ^String path ^String type]
  (let [f (File. path)]
    (ImageIO/write img type f)))

(defn- initial-state-atom
  [w h]
  (atom {:stack   []
         :current {:line-style (core/line-style)
                   :fill-style (core/fill-style)
                   :transform  (AffineTransform.)
                   :clipping   nil}}))

(defn draw
  [^BufferedImage img f]
  (let [^Graphics2D g (.getGraphics img)]
    (doto g
      (.setRenderingHint RenderingHints/KEY_ANTIALIASING RenderingHints/VALUE_ANTIALIAS_ON)
      (.setRenderingHint RenderingHints/KEY_STROKE_CONTROL RenderingHints/VALUE_STROKE_PURE))
    (let [ctx (->JavaAwtContext img g (initial-state-atom (.getWidth img) (.getHeight img)))]
      (f ctx))
    (.dispose g)))

(defn draw-in-file
  [path type w h f]
  (let [img (create-image w h)]
    (draw img f)
    (save-image img path type)))
