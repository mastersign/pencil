(ns net.kiertscher.draw.pencil.jvm-awt
  (:require [net.kiertscher.draw.pencil :as core])
  (:import [java.io File]
           [java.awt Graphics2D Color BasicStroke AlphaComposite RenderingHints Shape]
           [java.awt.image BufferedImage]
           [java.awt.geom Line2D$Double
                          Rectangle2D$Double
                          Ellipse2D$Double
                          Arc2D$Double
                          QuadCurve2D$Double
                          CubicCurve2D$Double
                          Path2D$Double
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
  (Color. ^int (convert-color-value (get c :r 0))
          ^int (convert-color-value (get c :g 0))
          ^int (convert-color-value (get c :b 0))
          ^int (convert-color-value (get c :a 1))))

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

(defn- update-current-state-f
  [ctx k f]
  (swap! (:state ctx) (fn [s] (assoc-in s [:current k] (f (get-in s [:current k]))))))

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

(defn- ^Shape build-ellipse
  ([x y rx ry]
   (Ellipse2D$Double. (- x rx) (- y ry) (* 2 rx) (* 2 ry)))
  ([x y rx ry start extend]
   (build-ellipse x y rx ry start extend :open))
  ([x y rx ry start extend typ]
   (if (>= (Math/abs ^double extend) (* 2 Math/PI))
     (build-ellipse x y rx ry)
     (Arc2D$Double. (- x rx) (- y ry) (* 2 rx) (* 2 ry)
                    (* -180.0 (/ start Math/PI))
                    (* -180.0 (/ extend Math/PI))
                    (case typ
                      :pie Arc2D$Double/PIE
                      :chord Arc2D$Double/CHORD
                      Arc2D$Double/OPEN)))))

(defn- build-path
  ([ctx]
   (build-path ctx :non-zero))
  ([ctx rule]
   (let [ops (get-current-state ctx :path)
         rule' (if (= rule :even-odd)
                 Path2D$Double/WIND_EVEN_ODD
                 Path2D$Double/WIND_NON_ZERO)
         p (Path2D$Double. rule')]
     (doseq [{:keys [cmd] :as op} ops]
       (case cmd
         :close (.closePath p)
         :move-to (let [{:keys [x y]} op]
                    (.moveTo p x y))
         :line-to (let [{:keys [x y]} op]
                    (.lineTo p x y))
         :quad-curve-to (let [{:keys [cx cy x y]} op]
                          (.quadTo p cx cy x y))
         :cubic-curve-to (let [{:keys [cx1 cy1 cx2 cy2 x y]} op]
                           (.curveTo p cx1 cy1 cx2 cy2 x y))
         :arc (let [{:keys [x y rx ry start extend]} op]
                (.append p
                         (build-ellipse x y rx ry start extend)
                         (< (Math/abs ^double extend) (* 2.0 Math/PI))))))
     p)))

(defrecord JavaAwtContext
  [^BufferedImage img
   ^Graphics2D g
   state]

  core/IInformation

  (canvas-size [_]
    [(.getWidth img)
     (.getHeight img)])

  core/IClearing

  (clear-rect [_ x y w h]
    (clear-rect g x y w h))

  (clear-rect [_ x y w h c]
    (clear-rect g x y w h)
    (.setColor g (convert-color c))
    (.fillRect g x y w h))

  (clear-all [_]
    (let [w (.getWidth img)
          h (.getHeight img)]
      (clear-rect g 0 0 w h)))
  
  (clear-all [_ c]
    (let [w (.getWidth img)
          h (.getHeight img)]
      (clear-rect g 0 0 w h)
      (.setColor g (convert-color c))
      (.fillRect g 0 0 w h)))

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
    (let [s (build-ellipse x y r r)]
      (.draw g s)))

  (draw-arc [ctx x y r start extend]
    (apply-current-line-style ctx)
    (let [s (build-ellipse x y r r start extend)]
      (.draw g s)))

  (draw-ellipse [ctx x y rx ry]
    (apply-current-line-style ctx)
    (let [s (build-ellipse x y rx ry)]
      (.draw g s)))

  (draw-ellipse [ctx x y rx ry start extend]
    (apply-current-line-style ctx)
    (let [s (build-ellipse x y rx ry start extend)]
      (.draw g s)))

  (draw-quadratic-curve [ctx x1 y1 cx cy x2 y2]
    (apply-current-line-style ctx)
    (let [s (QuadCurve2D$Double. x1 y1 cx cy x2 y2)]
      (.draw g s)))

  (draw-cubic-curve [ctx x1 y1 cx1 cy1 cx2 cy2 x2 y2]
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
    (let [s (build-ellipse x y r r)]
      (.fill g s)))

  (fill-arc [ctx x y r start extend]
    (apply-current-fill-style ctx)
    (let [s (build-ellipse x y r r start extend :pie)]
      (.fill g s)))

  (fill-ellipse [ctx x y rx ry]
    (apply-current-fill-style ctx)
    (let [s (Ellipse2D$Double. (- x rx) (- y ry) (* 2 rx) (* 2 ry))]
      (.fill g s)))

  (fill-ellipse [ctx x y rx ry start extend]
    (apply-current-fill-style ctx)
    (let [s (build-ellipse x y rx ry start extend :pie)]
      (.fill g s)))

  core/IPathRendering

  (begin-path [ctx]
    (update-current-state ctx :path []))

  (path-close [ctx]
    (update-current-state-f
      ctx :path #(conj % {:cmd :close})))

  (path-move-to [ctx x y]
    (update-current-state-f
      ctx :path #(conj % {:cmd :move-to
                          :x   x :y y})))

  (path-line-to [ctx x y]
    (update-current-state-f
      ctx :path #(conj % {:cmd :line-to
                          :x   x :y y})))

  (path-quadratic-curve-to [ctx cx cy x y]
    (update-current-state-f
      ctx :path #(conj % {:cmd :quad-curve-to
                          :cx  cx :cy cy
                          :x   x :y y})))

  (path-cubic-curve-to [ctx cx1 cy1 cx2 cy2 x y]
    (update-current-state-f
      ctx :path #(conj % {:cmd :cubic-curve-to
                          :cx1 cx1 :cy1 cy1
                          :cx2 cx2 :cy2 cy2
                          :x   x :y y})))

  (path-arc [ctx x y r]
    (update-current-state-f
      ctx :path #(conj % {:cmd   :arc
                          :x     x :y y
                          :rx    r :ry r
                          :start 0.0 :extend (* 2.0 Math/PI)})))

  (path-arc [ctx x y r start extend]
    (update-current-state-f
      ctx :path #(conj % {:cmd   :arc
                          :x     x :y y
                          :rx    r :ry r
                          :start start :extend extend})))

  (path-ellipse [ctx x y rx ry]
    (update-current-state-f
      ctx :path #(conj % {:cmd   :arc
                          :x     x :y y
                          :rx    rx :ry ry
                          :start 0.0 :extend (* 2.0 Math/PI)})))

  (path-ellipse [ctx x y rx ry start extend]
    (update-current-state-f
      ctx :path #(conj % {:cmd   :arc
                          :x     x :y y
                          :rx    rx :ry ry
                          :start start :extend extend})))

  (draw-path [ctx]
    (apply-current-line-style ctx)
    (.draw g (build-path ctx)))

  (fill-path [ctx]
    (apply-current-fill-style ctx)
    (.fill g (build-path ctx)))

  (fill-path [ctx rule]
    (apply-current-fill-style ctx)
    (.fill g (build-path ctx rule)))

  (clip-path [ctx]
    (.clip g (build-path ctx)))

  (clip-path [ctx rule]
    (.clip g (build-path ctx rule)))

  core/ITransforming

  (set-transform [ctx a b c d e f]
    (let [t (AffineTransform. ^double a ^double b
                              ^double c ^double d
                              ^double e ^double f)]
      (.setTransform g t)
      (update-current-state ctx :transform t)))

  (reset-transform [ctx]
    (let [t-eye (AffineTransform.)]
      (.setTransform g t-eye)
      (update-current-state ctx :transform t-eye)))

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
    (.setClip g (get-current-state ctx :clipping)))

  core/IRegion

  (set-region [ctx x y w h]
    (update-current-state ctx :region {:x x :y y :width w :height h}))

  (get-region [ctx]
    (get-current-state ctx :region)))


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
                   :clipping   nil
                   :region     {:x 0 :y 0 :width w :height h}
                   :path       []}}))

(defn render
  [^BufferedImage img f]
  (let [^Graphics2D g (.getGraphics img)]
    (doto g
      (.setRenderingHint RenderingHints/KEY_ANTIALIASING RenderingHints/VALUE_ANTIALIAS_ON)
      (.setRenderingHint RenderingHints/KEY_STROKE_CONTROL RenderingHints/VALUE_STROKE_PURE))
    (let [ctx (->JavaAwtContext img g (initial-state-atom (.getWidth img) (.getHeight img)))]
      (f ctx))
    (.dispose g)))

(defn render-in-file
  [path type w h f]
  (let [img (create-image w h)]
    (render img f)
    (save-image img path type)))
