Plots
=====

Two-dimensional plots

Time-series plot example
------------------------

`axle.visualize.Plot`

```scala
scala> import axle._
import axle._

scala> import axle.visualize._
import axle.visualize._

scala> import org.joda.time.DateTime
import org.joda.time.DateTime

scala> import spire.compat.ordering
import spire.compat.ordering

scala> import scala.collection.immutable.TreeMap
import scala.collection.immutable.TreeMap

scala> import scala.math.sin
import scala.math.sin

scala> import scala.util.Random.nextDouble
import scala.util.Random.nextDouble

scala> import axle.joda.dateTimeOrder
import axle.joda.dateTimeOrder

scala> val now = new DateTime()
now: org.joda.time.DateTime = 2016-08-18T17:51:01.131-07:00

scala> def randomTimeSeries(i: Int) = {
     |   val φ = nextDouble
     |   val A = nextDouble
     |   val ω = 0.1 / nextDouble
     |   ("%1.2f %1.2f %1.2f".format(φ, A, ω),
     |     new TreeMap[DateTime, Double]() ++
     |     (0 to 100).map(t => (now.plusMinutes(2 * t) -> A * sin(ω*t + φ))).toMap)
     | }
randomTimeSeries: (i: Int)(String, scala.collection.immutable.TreeMap[org.joda.time.DateTime,Double])

scala> val waves = (0 until 20).map(randomTimeSeries)
waves: scala.collection.immutable.IndexedSeq[(String, scala.collection.immutable.TreeMap[org.joda.time.DateTime,Double])] = Vector((0.44 0.49 0.17,Map(2016-08-18T17:51:01.131-07:00 -> 0.20973310460997982, 2016-08-18T17:53:01.131-07:00 -> 0.28211755473060746, 2016-08-18T17:55:01.131-07:00 -> 0.3463710607149205, 2016-08-18T17:57:01.131-07:00 -> 0.40064176450247446, 2016-08-18T17:59:01.131-07:00 -> 0.44336552358127107, 2016-08-18T18:01:01.131-07:00 -> 0.4733109913281103, 2016-08-18T18:03:01.131-07:00 -> 0.489615105798414, 2016-08-18T18:05:01.131-07:00 -> 0.49180796413882494, 2016-08-18T18:07:01.131-07:00 -> 0.4798263657141265, 2016-08-18T18:09:01.131-07:00 -> 0.4540156336203876, 2016-08-18T18:11:01.131-07:00 -> 0.4151196620862818, 2016-08-18T18:13:01.131-07:00 -> 0.36425947660762936, 2016-...

scala> import axle.joda.dateTimeZero
import axle.joda.dateTimeZero

scala> implicit val zeroDT = dateTimeZero(now)
zeroDT: axle.algebra.Zero[org.joda.time.DateTime] = axle.joda.package$$anon$3@54a2d1f5

scala> import axle.visualize.Plot
import axle.visualize.Plot

scala> import spire.implicits.DoubleAlgebra
import spire.implicits.DoubleAlgebra

scala> import axle.algebra.Plottable.doublePlottable
import axle.algebra.Plottable.doublePlottable

scala> import axle.joda.dateTimeOrder
import axle.joda.dateTimeOrder

scala> import axle.joda.dateTimePlottable
import axle.joda.dateTimePlottable

scala> import axle.joda.dateTimeTics
import axle.joda.dateTimeTics

scala> import axle.joda.dateTimeDurationLengthSpace
import axle.joda.dateTimeDurationLengthSpace

scala> val plot = Plot(
     |   waves,
     |   title = Some("Random Waves"),
     |   xAxis = Some(0d),
     |   xAxisLabel = Some("time (t)"),
     |   yAxisLabel = Some("A sin(ωt + φ)"))
plot: axle.visualize.Plot[org.joda.time.DateTime,Double,scala.collection.immutable.TreeMap[org.joda.time.DateTime,Double]] = Plot(Vector((0.44 0.49 0.17,Map(2016-08-18T17:51:01.131-07:00 -> 0.20973310460997982, 2016-08-18T17:53:01.131-07:00 -> 0.28211755473060746, 2016-08-18T17:55:01.131-07:00 -> 0.3463710607149205, 2016-08-18T17:57:01.131-07:00 -> 0.40064176450247446, 2016-08-18T17:59:01.131-07:00 -> 0.44336552358127107, 2016-08-18T18:01:01.131-07:00 -> 0.4733109913281103, 2016-08-18T18:03:01.131-07:00 -> 0.489615105798414, 2016-08-18T18:05:01.131-07:00 -> 0.49180796413882494, 2016-08-18T18:07:01.131-07:00 -> 0.4798263657141265, 2016-08-18T18:09:01.131-07:00 -> 0.4540156336203876, 2016-08-18T18:11:01.131-07:00 -> 0.4151196620862818, 2016-08-18T18:13:01.131-07:00 -> 0.36425947660762936,...

scala> import axle.web._
import axle.web._

scala> svg(plot, "waves.svg")
```

![waves](../images/waves.svg)

Animation
---------

This example traces two "saw" functions vs time:

```scala
import collection.immutable.TreeMap
import org.joda.time.DateTime
import axle.joda._
import spire.compat.ordering

val initialData = List(
  ("saw 1", new TreeMap[DateTime, Double]()),
  ("saw 2", new TreeMap[DateTime, Double]())
)

import spire.implicits.DoubleAlgebra
import axle.visualize._

val now = new DateTime()
implicit val dtz = dateTimeZero(now)

val plot = Plot[DateTime, Double, TreeMap[DateTime, Double]](
  initialData,
  connect = true,
  title = Some("Saws"),
  xAxis = Some(0d),
  xAxisLabel = Some("time (t)"),
  yAxisLabel = Some("y")
)

val saw1 = (t: Long) => (t % 10000) / 10000d
val saw2 = (t: Long) => (t % 100000) / 50000d

val fs = List(saw1, saw2)

val refreshFn = (previous: List[(String, TreeMap[DateTime, Double])]) => {
  val now = new DateTime()
  previous.zip(fs).map({ case (old, f) => (old._1, old._2 ++ Vector(now -> f(now.getMillis))) })
}

import akka.actor.ActorSystem
implicit val system = ActorSystem("Animator")

import axle.jung._
import axle.quanta.Time
import edu.uci.ics.jung.graph.DirectedSparseGraph

implicit val timeConverter = {
  import axle.algebra.modules.doubleRationalModule
  Time.converterGraphK2[Double, DirectedSparseGraph]
}
import timeConverter.millisecond

play(plot, refreshFn, 500 *: millisecond)
```