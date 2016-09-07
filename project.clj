(defproject pencil "0.1.0-SNAPSHOT"
  :description "A lightweight 2D graphics library for Clojure and ClojureScript"
  :url "https://github.com/mastersign/pencil"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.227"]]
  :plugins [[lein-cljsbuild "1.1.4"]]
  :cljsbuild {:builds [{:id "pencil"
                        :source-paths ["src" "test"]
                        :compiler {:output-to "out/js/pencil.js"
                                   :optimizations :whitespace}}]}
  :source-paths ["src"])
