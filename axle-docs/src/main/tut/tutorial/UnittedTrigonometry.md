---
layout: page
title: Unitted Trigonometry
permalink: /tutorial/unitted_trigonometry/
---

Versions of the trigonometric functions sine, cosine, and tangent, require that the arguments are Angles.

Examples
--------

Examples of the functions

Imports and implicits

```tut:book:silent
import axle._
import axle.quanta.Angle
import axle.quanta.UnittedQuantity
import axle.quanta.UnitOfMeasurement
import axle.algebra.modules.doubleRationalModule
import spire.implicits.DoubleAlgebra
import spire.algebra.Module
import spire.math.Rational
import axle.jung.directedGraphJung
import edu.uci.ics.jung.graph.DirectedSparseGraph

implicit val angleConverter = Angle.converterGraphK2[Double, DirectedSparseGraph]

import angleConverter.degree
import angleConverter.radian
```

Usage

```tut:book
cosine(10d *: degree)

sine(3d *: radian)

tangent(40d *: degree)
```