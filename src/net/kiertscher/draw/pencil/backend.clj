(ns net.kiertscher.draw.pencil.backend
  (:require [net.kiertscher.draw.pencil.core :refer [IGraphicsContext]])
  (:import [java.io File]
           [java.awt Graphics2D Color]
           [java.awt.image BufferedImage]
           [java.awt.geom Rectangle2D$Float]
           [javax.imageio ImageIO]
           [javax.swing JOptionPane]))

(defn info
  "Display an info message."
  [txt]
  (JOptionPane/showMessageDialog
    nil txt "Info Message" JOptionPane/INFORMATION_MESSAGE))

(defrecord JavaAwtContext
  [^BufferedImage img
   ^Graphics2D g]

  IGraphicsContext

  (draw-rect [_ x y w h]
    (let [s (Rectangle2D$Float. x y w h)]
      (.draw g s)))

  (fill-rect [_ x y w h]
    (let [s (Rectangle2D$Float. x y w h)]
      (.fill g s))))

(defn create-image
  [w h]
  (BufferedImage. w h BufferedImage/TYPE_INT_ARGB))

(defn open-image-context
  [^BufferedImage img]
  (let [g (.getGraphics img)]
    (.setColor g Color/BLUE)
    (->JavaAwtContext img g)))

(defn save-image
  [^BufferedImage img ^String path ^String type]
  (let [f (File. path)]
    (ImageIO/write img type f)))

(defn draw-in-file
  [path type w h f]
  (let [img (create-image w h)
        ctx (open-image-context img)]
    (f ctx)
    (save-image img path type)))
