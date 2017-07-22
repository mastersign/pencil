(defproject pencil "0.1.0-SNAPSHOT"
  :description "A lightweight 2D graphics library for Clojure and ClojureScript"
  :url "https://github.com/mastersign/pencil"
  :license {:name "MIT"
            :url  "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.229"]]
  :plugins [[lein-cljsbuild "1.1.6"]
            [lein-codox "0.10.3"]]
  :profiles {:doc-clj  {:codox {:language     :clojure
                                :source-paths ["src/clj"]
                                :project      {:name "Pencil - Clojure for JVM only API"}
                                :namespaces   [net.kiertscher.draw.pencil.jvm-awt]
                                :output-path  "target/doc/clj"}}
             :doc-cljs {:codox {:language     :clojurescript
                                :source-paths ["src/cljs"]
                                :project      {:name "Pencil - ClojureScript only API"}
                                :namespaces   [net.kiertscher.draw.pencil.js-canvas]
                                :output-path  "target/doc/cljs"}}}
  :cljsbuild {:builds
              {:prod {:source-paths ["src/cljc" "src/cljs"]
                      :compiler     {:output-to     "out/js/pencil.js"
                                     :optimizations :advanced}}
               :dev  {:source-paths ["src/cljc" "src/cljs" "test/cljc" "test/cljs"]
                      :compiler     {:optimizations :none
                                     :main          "net.kiertscher.draw.js-canvas-test"
                                     :output-dir    "out/js"
                                     :output-to     "out/js/pencil-test.js"
                                     :asset-path    "js"
                                     :pretty-print  true}}}}
  :source-paths ["src/cljc" "src/clj" "src/cljs"]
  :test-paths ["test/cljc" "test/clj"]
  :output-dir ["target"]
  :codox {:clojure      :clojure
          :source-paths ["src/cljc"]
          :source-uri   "https://github.com/mastersign/pencil/blob/{version}/{filepath}#L{line}"
          :output-path  "target/doc/cljc"
          :html         {:namespace-list :flat}})
