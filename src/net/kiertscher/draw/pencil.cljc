(ns net.kiertscher.draw.pencil
  (:require [net.kiertscher.draw.pencil.backend :as p]))

(defn main-test
  "I call a function, defined in different languages."
  []
  (p/info "Drawing with a pencil."))

#?(:cljs (main-test))
