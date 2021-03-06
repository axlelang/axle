---
layout: page
title: Unitted Trigonometry
permalink: /tutorial/unitted_trigonometry/
---

Versions of the trigonometric functions sine, cosine, and tangent, require that the arguments are Angles.

## Examples

Examples of the functions

Imports and implicits

```scala mdoc:silent
import edu.uci.ics.jung.graph.DirectedSparseGraph

import cats.implicits._

import spire.algebra.Field
import spire.algebra.Trig

import axle.math._
import axle.quanta.Angle
import axle.quanta.UnitOfMeasurement
import axle.algebra.modules.doubleRationalModule
import axle.jung.directedGraphJung

implicit val fieldDouble: Field[Double] = spire.implicits.DoubleAlgebra
implicit val trigDouble: Trig[Double] = spire.implicits.DoubleAlgebra

implicit val angleConverter = Angle.converterGraphK2[Double, DirectedSparseGraph]

import angleConverter.degree
import angleConverter.radian
```

Usage

```scala mdoc
cosine(10d *: degree)

sine(3d *: radian)

tangent(40d *: degree)
```
