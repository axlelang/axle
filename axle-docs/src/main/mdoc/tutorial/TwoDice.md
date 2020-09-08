---
layout: page
title: Two Dice
permalink: /tutorial/two_dice/
---

Combining probability models representing rolls of 6-sided dice demonstrate
some behavior of `ProbabilityModel`.

Setup

```scala mdoc
import cats.implicits._
import axle.eqSymbol
import axle.probability._
import axle.game.Dice._
import axle.syntax.probabilitymodel._

implicit val prob = ProbabilityModel[ConditionalProbabilityTable]
```

## Monadic map to operate on the event space

Map the model on the integers `1 to 6` to a model of UTF
symbols representing the faces of the dice.

```scala mdoc
val d6utf = die(6).map(numberToUtfFace)
```

Monadic `flatMap` constructs a model of the sequence of two rolls

```scala mdoc
val bothDieModel = d6utf.flatMap({ flip1 =>
  d6utf.map({ flip2 => (flip1, flip2) })
})
```

Query the resulting probability model's distribution of 2-roll events.

```scala mdoc
import axle.algebra._ // for Region*

type TWOROLLS = (Symbol, Symbol)

bothDieModel.P(RegionIf[TWOROLLS](_._1 == Symbol("⚃")) and RegionIf[TWOROLLS](_._2 == Symbol("⚃")))

bothDieModel.P(RegionNegate(RegionIf[TWOROLLS](_._1 == Symbol("⚃"))))
```

Observe rolls of a die

```scala mdoc
import spire.random.Generator.rng

implicit val dist = axle.probability.rationalProbabilityDist

(1 to 10) map { i => d6utf.observe(rng) }
```

## Simulate the sum of two dice

Compare two methods of computing distributions -- simulation and full, precise construction using monads.

The following code shows the simulation of 1,000 2-dice sums.

```scala mdoc:silent
import cats.implicits._

import spire.algebra._
import spire.math.Rational

import axle.game.Dice.die
import axle.syntax.talliable.talliableOps
```

Simulate 10k rolls of two dice

```scala mdoc
val seed = spire.random.Seed(42)
val gen = spire.random.Random.generatorFromSeed(seed)
val d6 = die(6)
val rolls = (0 until 1000) map { i => d6.observe(gen) + d6.observe(gen) }

implicit val ringInt: CRing[Int] = spire.implicits.IntAlgebra

val histogram = rolls.tally
```

Define visualization

```scala mdoc:silent
import axle.visualize._
```

```scala mdoc
val chart = BarChart[Int, Int, Map[Int, Int], String](
  () => histogram,
  colorOf = _ => Color.blue,
  xAxis = Some(0),
  title = Some("d6 + d6"),
  labelAngle = Some(0d *: angleDouble.degree),
  drawKey = false)
```

Create SVG

```scala mdoc
import cats.effect._
import axle.web._

chart.svg[IO]("d6plusd6.svg").unsafeRunSync()
```

![Observed d6 + d6](/tutorial/images/d6plusd6.svg)

## Direct computation of the sum of two dice using monads

The full distribution of two rolls combined can be computed directly and precisely
using monads.  Of course this does become infeasable as the models are combined -- 
axle will have more on that tradeoff in future versions.

Imports (Note: documentation resets interpreter here)

```scala mdoc:silent:reset
import spire.math._

import axle.syntax.probabilitymodel._
import axle.probability._
import axle.game.Dice.die
```

## Monadic flatMap

Create probability distribution of the addition of two 6-sided die:

```scala mdoc
implicit val prob = ProbabilityModel[ConditionalProbabilityTable]

implicit val intEq: cats.kernel.Eq[Int] = spire.implicits.IntAlgebra

val twoDiceSummed = die(6).flatMap { a =>
  die(6).map { b => a + b }
}
```

Define visualization

```scala mdoc:silent
import axle.visualize._
```

```scala mdoc
import cats.implicits._

val chart = BarChart[Int, Rational, ConditionalProbabilityTable[Int, Rational], String](
  () => twoDiceSummed,
  colorOf = _ => Color.blue,
  xAxis = Some(Rational(0)),
  title = Some("d6 + d6"),
  labelAngle = Some(0d *: angleDouble.degree),
  drawKey = false)
```

Create SVG

```scala mdoc
import cats.effect._
import axle.web._

chart.svg[IO]("distributionMonad.svg").unsafeRunSync()
```

![Monadic d6 + d6](/tutorial/images/distributionMonad.svg)
