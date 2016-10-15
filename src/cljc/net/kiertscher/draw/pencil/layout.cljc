(ns net.kiertscher.draw.pencil.layout
  (:require [net.kiertscher.draw.pencil :as p]))

(defprotocol ILayout
  (apply-layout [_ ctx]))

(defn render-with-layout
  [ctx layout f]
  (p/push-state ctx)
  (p/reset-transform ctx)
  (apply-layout layout ctx)
  (f ctx)
  (p/pop-state ctx))

(defn- complete-border*
  ([] [0.0 0.0 0.0 0.0])
  ([v] (if (coll? v)
         (apply complete-border* v)
         [v v v v]))
  ([x y] [x y x y])
  ([left y right] [left y right y])
  ([left top right bottom] [left top right bottom]))

(defn- complete-spacing*
  ([] [0.0 0.0])
  ([v] (if (coll? v)
         (apply complete-spacing* v)
         [v v]))
  ([x y] [x y]))

(defrecord Table
  [columns                                                  ;; the number of columns
   rows                                                     ;; the number of rows
   margin                                                   ;; a vector with outer margin [l t r b]
   cell-spacing])                                           ;; a vector with cell-spacing [x y]

(def ^:dynamic *default-table*
  {:columns      1
   :rows         1
   :margin       0.0
   :cell-spacing 0.0})

(defn table
  ([] (table *default-table*))
  ([m]
   (let [{c  :columns
          r  :rows
          m  :margin
          cs :cell-spacing}
         (merge *default-table* m)]
     (Table. c r (complete-border* m) (complete-spacing* cs))))
  ([k v & kvps]
   (table (apply hash-map k v kvps))))

(defrecord Axis
  [range-x                                                  ;; vector with left and right
   range-y                                                  ;; vector with top and bottom
   align-x                                                  ;; :center, :near, :far, :stretch
   align-y])                                                ;; :center, :near, :far, :stretch

(def ^:dynamic *default-axis*
  {:range-x [-1.0 +1.0]
   :range-y [+1.0 -1.0]
   :align-x :stretch
   :align-y :stretch})

(defn axis
  ([] (axis *default-axis*))
  ([m]
   (let [{rx :range-x
          ry :range-y
          ax :align-x
          ay :align-y}
         (merge *default-axis* m)]
     (Axis. rx ry ax ay)))
  ([k v & kvps]
   (axis (apply hash-map k v kvps))))

(defn- axis-transform [s1 s2 v1 v2 d a1 a2]
  (let [sign (fn [v] (if (< v 0.0) -1.0 1.0))]
    (if (= a1 :stretch)
      [(* s1 (- v1)) s1]
      (let [s (if (= a2 :stretch)
                (* (sign ^double s1) (Math/abs ^double s2))
                (* (sign ^double s1) (min (Math/abs ^double s1) (Math/abs ^double s2))))]
        (case a1
          :near [(* s (- v1)) s]
          :center [(- (* 0.5 d) (* s (+ v1 (* 0.5 (- v2 v1)))))
                   s]
          :far [(- d (* s v2)) s]
          [0 0])))))

(defn- axis-transform-2
  [cw ch {[x-l x-r] :range-x
          [y-t y-b] :range-y
          ax        :align-x
          ay        :align-y}]
  (let [adx (- x-r x-l)
        ady (- y-b y-t)
        sx (/ cw adx)
        sy (/ ch ady)]
    (let [[tx sx'] (axis-transform sx sy x-l x-r cw ax ay)
          [ty sy'] (axis-transform sy sx y-t y-b ch ay ax)]
      [tx ty sx' sy'])))

(defrecord TableCellLayout
  [table                                                    ;; a Table instance
   axis                                                     ;; an Axis instance
   column                                                   ;; the start column of the cell
   row                                                      ;; the start row of the cell
   column-span                                              ;; the number of columns spanned by the cell
   row-span                                                 ;; the number of rows spanned by the cell
   clip]                                                    ;; clip mode :none, :cell, :axis

  ILayout

  (apply-layout [_ ctx]
    (let [[can-w can-h] (p/canvas-size ctx)
          cols (:columns table)                             ;; number of columns
          rows (:rows table)                                ;; number of rows
          [ml mt mr mb] (:margin table)                     ;; outer margin on x-axis
          [csx csy] (:cell-spacing table)                   ;; spacing between cells on x-axis
          dx (+ ml mr (* (dec cols) csx))                   ;; total margin and spacing on x-axis
          dy (+ mt mb (* (dec rows) csy))                   ;; total margin and spacing on y-axis
          cw (/ (- can-w dx) cols)                          ;; cell width
          ch (/ (- can-h dy) rows)                          ;; cell height
          ccw (+ (* cw column-span)                         ;; current cell width
                 (* csx (dec column-span)))
          cch (+ (* ch row-span)                            ;; current cell height
                 (* csy (dec row-span)))
          ccx (+ ml (* (+ cw csx) column))                  ;; current cell left
          ccy (+ mt (* (+ ch csy) row))]                    ;; current cell top
      (p/translate ctx ccx ccy)
      (when (= clip :cell)
        (p/clip-rect ctx 0 0 ccw cch))
      (when axis
        (let [[tx ty sx sy] (axis-transform-2 ccw cch axis)]
          (p/translate ctx tx ty)
          (p/scale ctx sx sy)
          (when (= clip :axis)
            (let [{[x-l x-r] :range-x
                   [y-t y-b] :range-y} axis]
              (p/clip-rect ctx
                           (- x-l) (- y-t)
                           (- x-r x-l) (- y-b y-t)))))))))

(def ^:dynamic *default-table-cell-layout*
  {:table       (table)
   :axis        nil
   :column      0
   :row         0
   :column-span 1
   :row-span    1
   :clip        :cell})

(defn table-cell-layout
  ([] (table-cell-layout *default-table-cell-layout*))
  ([m]
   (let [{t  :table
          a  :axis
          c  :column
          r  :row
          cs :column-span
          rs :row-span
          cl :clip}
         (merge *default-table-cell-layout* m)]
     (TableCellLayout. t a c r cs rs cl)))
  ([k v & kvps]
   (table-cell-layout (apply hash-map k v kvps))))
