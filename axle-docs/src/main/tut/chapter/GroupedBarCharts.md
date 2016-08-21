---
layout: page
title: Grouped Bar Charts
permalink: /chapter/grouped_bar_charts/
---

Two-dimensional grouped bar charts

Example
-------

The following example dataset:

```tut:book
val sales = Map(
  ("apple", 2011) -> 43.0,
  ("apple", 2012) -> 83.8,
  ("banana", 2011) -> 11.3,
  ("banana", 2012) -> 77.9,
  ("coconut", 2011) -> 88.0,
  ("coconut", 2012) -> 10.1
)
```

Can be grouped in two ways to produce bar charts:

```tut:book
import spire.implicits.DoubleAlgebra
import spire.implicits.IntAlgebra
import spire.implicits.StringOrder
import axle.visualize.BarChartGrouped

val chart = BarChartGrouped[String, Int, Double, Map[(String, Int), Double]](
  sales,
  title = Some("fruit sales")
)

import axle.web._
svg(chart, "barchart1.svg")
```

![barchart1](/chapter/images/barchart1.svg)

Or:

```tut:book
import spire.implicits.DoubleAlgebra

val chart = BarChartGrouped[Int, String, Double, Map[(Int, String), Double]](
  sales map { case (k, v) => (k._2, k._1) -> v},
  title = Some("fruit sales")
)

import axle.web._
svg(chart, "barchart2.svg")
```

![barchart2](/chapter/images/barchart2.svg)

Animation
---------
This example keeps the "bar" value steady at 1.0 while assigning a new random Double (between 0 and 1) to "foo" every second.

```scala
val groups = Vector("foo", "bar")
val initial = Map("foo" -> 1d, "bar" -> 1d)

import axle.visualize._
import spire.implicits._
 
val chart = BarChart[String, Double, Map[String, Double]](
  initial,
  title = Some("random")
)

import scala.util.Random.nextDouble
val tick = (previous: Map[String, Double]) => previous + ("foo" -> nextDouble)

import akka.actor.ActorSystem
implicit val system = ActorSystem("Animator")

import axle.jung._
import axle.quanta.Time

implicit val timeConverter = {
  import axle.algebra.modules.doubleRationalModule
  Time.converterGraphK2[Double, DirectedSparseGraph]
}
import timeConverter.second

play(chart, tick, 1d *: second)
```