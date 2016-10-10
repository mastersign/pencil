(ns net.kiertscher.draw.pencil.jvm-awt-test
  (:require
    [net.kiertscher.draw.pencil-test :as t]
    [net.kiertscher.draw.pencil.jvm-awt :as awt])
  (:import [java.io File]))

(defn draw
  [id w h f]
  (.mkdirs (File. "out/test/awt"))
  (awt/render-in-file
    (str "out/test/awt/" id ".png") "PNG"
    w h f)  )

(doseq [[id {:keys [w h f]}] t/test-sketches]
  (draw (name id) w h f))
