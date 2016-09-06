(ns net.kiertscher.draw.pencil
  (:require [net.kiertscher.draw.pencil.core :as p]
            [net.kiertscher.draw.pencil.backend :as be]))

(defn main-test
  "I call a function, defined in different languages."
  []
  (be/info "Drawing with a pencil."))

(defn my-draw-f [ctx]
  (p/set-line-style ctx (p/line-style 2.0 (p/color 1.0 0.0 0.5)))
  (p/set-fill-style ctx (p/fill-style (p/color 0.5 0.5)))
  (p/fill-rect ctx 10 10 180 180)
  (p/fill-rect ctx 50 50 100 100)
  (doseq [v (range 20 60 5)]
    (p/draw-rect ctx v v v v))
  (p/clear-rect ctx 100 100 50 50))

#?(:cljs (be/draw-on-canvas "basics" my-draw-f))

#?(:clj (be/draw-in-file "out/tmp/temp.png" "PNG" 200 200 my-draw-f))
