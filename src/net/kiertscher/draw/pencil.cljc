(ns net.kiertscher.draw.pencil
  (:require [net.kiertscher.draw.pencil.core :as p]
            [net.kiertscher.draw.pencil.backend :as be]))

(defn main-test
  "I call a function, defined in different languages."
  []
  (be/info "Drawing with a pencil."))

(defn my-draw-f [ctx]
  (p/fill-rect ctx 40 30 10 20)
  (doseq [v (range 10 40 5)]
    (p/draw-rect ctx v v v v)))

#?(:cljs (be/draw-on-canvas "basics" my-draw-f))

#?(:clj (be/draw-in-file "out/tmp/temp.png" "PNG" 200 200 my-draw-f))
