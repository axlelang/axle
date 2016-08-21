---
layout: page
title: Bar Charts
permalink: /chapter/bar_charts/
---

Two-dimensional bar charts.

Example
-------

The dataset:

```tut:book
val sales = Map(
  "apple" -> 83.8,
  "banana" -> 77.9,
  "coconut" -> 10.1
)
```

Can be visualized as a bar chart with:

```tut:book
import spire.implicits.DoubleAlgebra
import spire.implicits.StringOrder
import axle.visualize.BarChart

val chart = BarChart[String, Double, Map[String, Double]](
  sales,
  title = Some("fruit sales")
)

import axle.web._
svg(chart, "fruitsales.svg")
```

![fruit sales](/chapter/images/fruitsales.svg)