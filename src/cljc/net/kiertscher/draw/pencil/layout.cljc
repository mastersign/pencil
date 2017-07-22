(ns net.kiertscher.draw.pencil.layout
  (:require [net.kiertscher.draw.pencil :as p]))

(defprotocol ILayout
  (apply-layout [_ ctx])

  (scale [_ x y]))

(defn render-with-layout
  [ctx layout f]
  (p/push-state ctx)
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

(defn- scale-border*
  [[left top right bottom] sx sy]
  [(* sx left)
   (* sy top)
   (* sx right)
   (* sy bottom)])

(defn- scale-spacing*
  [[x y] sx sy]
  [(* sx x)
   (* sy y)])

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

(defrecord CellBox
  [range-x                                                  ;; vector with left and right
   range-y                                                  ;; vector with top and bottom
   align-x                                                  ;; :center, :near, :far, :stretch
   align-y])                                                ;; :center, :near, :far, :stretch

(def ^:dynamic *default-box*
  {:range-x [-1.0 +1.0]
   :range-y [+1.0 -1.0]
   :align-x :stretch
   :align-y :stretch})

(defn box
  ([] (box *default-box*))
  ([m]
   (let [{rx :range-x
          ry :range-y
          ax :align-x
          ay :align-y}
         (merge *default-box* m)]
     (CellBox. rx ry ax ay)))
  ([k v & kvps]
   (box (apply hash-map k v kvps))))

(defn- box-transform [s1 s2 v1 v2 d a1 a2]
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

(defn- box-transform-2
  [cw ch {[x-l x-r] :range-x
          [y-t y-b] :range-y
          ax        :align-x
          ay        :align-y}]
  (let [adx (- x-r x-l)
        ady (- y-b y-t)
        sx (/ cw adx)
        sy (/ ch ady)]
    (let [[tx sx'] (box-transform sx sy x-l x-r cw ax ay)
          [ty sy'] (box-transform sy sx y-t y-b ch ay ax)]
      [tx ty sx' sy'])))

(defrecord TableCellLayout
  [table                                                    ;; a Table instance
   box                                                      ;; an Box instance
   column                                                   ;; the start column of the cell
   row                                                      ;; the start row of the cell
   column-span                                              ;; the number of columns spanned by the cell
   row-span                                                 ;; the number of rows spanned by the cell
   clip]                                                    ;; clip mode :none, :cell, :box

  ILayout

  (apply-layout [_ ctx]
    (let [{r-x :x r-y :y r-width :width r-height :height} (p/get-region ctx)
          cols (:columns table)                             ;; number of columns
          rows (:rows table)                                ;; number of rows
          [ml mt mr mb] (:margin table)                     ;; outer margin on x-box
          [csx csy] (:cell-spacing table)                   ;; spacing between cells on x-box
          dx (+ ml mr (* (dec cols) csx))                   ;; total margin and spacing on x-box
          dy (+ mt mb (* (dec rows) csy))                   ;; total margin and spacing on y-box
          cw (/ (- r-width dx) cols)                        ;; cell width
          ch (/ (- r-height dy) rows)                       ;; cell height
          ccw (+ (* cw column-span)                         ;; current cell width
                 (* csx (dec column-span)))
          cch (+ (* ch row-span)                            ;; current cell height
                 (* csy (dec row-span)))
          ccx (+ ml (* (+ cw csx) column))                  ;; current cell left
          ccy (+ mt (* (+ ch csy) row))]                    ;; current cell top
      (p/translate ctx ccx ccy)
      (p/set-region ctx 0 0 ccw cch)
      (when (or (= clip :cell) (and (not box) (= clip :box)))
        (p/clip-rect ctx 0 0 ccw cch))
      (when box
        (let [[tx ty sx sy] (box-transform-2 ccw cch box)]
          (p/translate ctx tx ty)
          (p/scale ctx sx sy)
          (let [{[x0 x1] :range-x
                 [y0 y1] :range-y} box]
            (p/set-region ctx x0 y0 (- x1 x0) (- y1 y0))
            (when (= clip :box)
              (p/clip-rect ctx x0 y0 (- x1 x0) (- y1 y0))))))))

  (scale [_ x y]
    (merge _ {:table (merge table {:margin       (scale-border* (:margin table) x y)
                                   :cell-spacing (scale-spacing* (:cell-spacing table) x y)})})))

(def ^:dynamic *default-table-cell-layout*
  {:table       (table)
   :box         nil
   :column      0
   :row         0
   :column-span 1
   :row-span    1
   :clip        :cell})

(defn table-cell-layout
  ([] (table-cell-layout *default-table-cell-layout*))
  ([m]
   (let [{t  :table
          a  :box
          c  :column
          r  :row
          cs :column-span
          rs :row-span
          cl :clip}
         (merge *default-table-cell-layout* m)]
     (TableCellLayout. t a c r cs rs cl)))
  ([k v & kvps]
   (table-cell-layout (apply hash-map k v kvps))))
