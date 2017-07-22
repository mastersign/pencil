(defproject pencil "0.1.0-SNAPSHOT"
  :description "A lightweight 2D graphics library for Clojure and ClojureScript"
  :url "https://github.com/mastersign/pencil"
  :license {:name "MIT"
            :url  "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.229"]]
  :plugins [[lein-cljsbuild "1.1.6"]
  :cljsbuild {:builds
              {:prod {:source-paths ["src/cljc" "src/cljs"]
                      :compiler     {:output-to     "out/js/pencil.js"
                                     :optimizations :advanced}}
               :dev  {:source-paths ["src/cljc" "src/cljs" "test/cljc" "test/cljs"]
                      :compiler      {:output-to     "out/js/pencil-test.js"
                                      :optimizations :whitespace}}}}
  :source-paths ["src/cljc" "src/clj" "src/cljs"]
  :test-paths ["test/cljc" "test/clj"]
  :output-dir ["target"])
