(ns net.kiertscher.draw.pencil.backend
  (:import [javax.swing JOptionPane]))

(defn info
  "Display an info message."
  [txt]
  (JOptionPane/showMessageDialog
    nil txt "Info Message" JOptionPane/INFORMATION_MESSAGE))
