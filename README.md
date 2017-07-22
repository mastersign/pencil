# pencil

[![Clojars Project][clojars-img]][clojars-url]

> A lightweight 2D graphics library for Clojure and ClojureScript.

## Usage

This library can be used with _Clojure_ for `java.awt.Graphics2D`
and with _ClojureScript_ for `CanvasRenderingContext2D`.

At first, add `[pencil "0.1.0"]` to the dependencies in your `project.clj`.

Then implement a sketch function:

```clj
(ns my-namespace.sketch
  (:require [net.kiertscher.draw.pencil :as pencil]))

(defn my-sketch
  [ctx]
  (p/draw-rect 10 10 80 60)
  (p/set-fill-style (p/fill-style (p/color 0.0 0.3 1.0)))
  (p/fill-arc 50 40 15))
```

### Clojure

Call `render-in-file` to render the sketch into an image file.

```clj
(ns my-namespace
  (:require [net.kiertscher.draw.pencil.jvm-awt :as pencil-awt]
            [my-namespace.sketch :as s]))

(defn -main
    []
    (pencil-awt/render-in-file
      "/target/path/my-sketch.png" "PNG"
      100 80 s/my-sketch))
```

### ClojureScript

Prepare your HTML5 page with a canvas element:

```html
<!DOCTYPE html>
<head>
<title>My Sketch</title>
</head>
<body>
<section>
    <h2>My Sketch</h2>
    <canvas id="my-sketch" width="100" height="80"></canvas>
</section>
<script type="application/javascript" src="js/my-sketch.js"></script>
</body>
```

Call the `render` function, passing the id of the canvas element:

```clj
(ns my-namespace
  (:require [net.kiertscher.draw.pencil.js-canvas :as pencil-jsc]
            [my-namespace.sketch :as s]))

(pencil-jsc/render "my-sketch" s/my-sketch)
```

## License

Copyright © 2016 Tobias Kiertscher <dev@mastersign.de>

Distributed under the Eclipse Public License either version 1.0 or
(at your option) any later version.

[clojars-img]: https://img.shields.io/clojars/v/pencil.svg
[clojars-url]: https://clojars.org/pencil
[latest release]: https://github.com/mastersign/pencil/releases/latest
