(ns net.kiertscher.draw.pencil.jvm-awt
  (:require [net.kiertscher.draw.pencil :as core])
  (:import [java.io File]
           [java.awt Graphics2D Color BasicStroke AlphaComposite]
           [java.awt.image BufferedImage]
           [java.awt.geom Rectangle2D$Float]
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

(defn- update-line-style
  [ctx]
  (let [g (:g ctx)
        {:keys [color width]} (:line-style (deref (:state ctx)))]
    (.setColor g (convert-color color))
    (.setStroke g (BasicStroke. width))))

(defn- update-fill-style
  [ctx]
  (let [g (:g ctx)
        {:keys [color]} (:fill-style (deref (:state ctx)))]
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
    (let [s' (core/make-up-line-style s)]
      (swap! state (fn [s] (assoc s :line-style s'))))
    ctx)

  (draw-rect [ctx x y w h]
    (update-line-style ctx)
    (let [s (Rectangle2D$Float. x y w h)]
      (.draw g s))
    ctx)

  core/IFilling

  (set-fill-style [ctx s]
    (let [s' (core/make-up-fill-style s)]
      (swap! state (fn [s] (assoc s :fill-style s'))))
    ctx)

  (fill-rect [ctx x y w h]
    (update-fill-style ctx)
    (let [s (Rectangle2D$Float. x y w h)]
      (.fill g s))
    ctx))

(defn create-image
  [w h]
  (BufferedImage. w h BufferedImage/TYPE_INT_ARGB))

(defn save-image
  [^BufferedImage img ^String path ^String type]
  (let [f (File. path)]
    (ImageIO/write img type f)))

(defn- initial-state-atom
  []
  (atom {:line-style (core/line-style)
         :fill-style (core/fill-style)}))

(defn draw
  [^BufferedImage img f]
  (let [g (.getGraphics img)]
    (.setColor g Color/BLUE)
    (let [ctx (->JavaAwtContext img g (initial-state-atom))]
      (f ctx))
    (.dispose g)))

(defn draw-in-file
  [path type w h f]
  (let [img (create-image w h)]
    (draw img f)
    (save-image img path type)))
