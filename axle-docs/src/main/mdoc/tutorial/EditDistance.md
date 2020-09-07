---
layout: page
title: Edit Distance
permalink: /tutorial/edit_distance/
---

See the Wikipedia page on [Edit distance](https://en.wikipedia.org/wiki/Edit_distance)

## Levenshtein

See the Wikipedia page on [Levenshtein distance](https://en.wikipedia.org/wiki/Levenshtein_distance)

Imports and implicits

```scala mdoc:silent
import org.jblas.DoubleMatrix

import cats.implicits._

import spire.algebra.Ring
import spire.algebra.NRoot

import axle._
import axle.nlp.Levenshtein
import axle.jblas._

implicit val ringInt: Ring[Int] = spire.implicits.IntAlgebra
implicit val nrootInt: NRoot[Int] = spire.implicits.IntAlgebra
implicit val laJblasInt = linearAlgebraDoubleMatrix[Int]
implicit val space = Levenshtein[IndexedSeq, Char, DoubleMatrix, Int]()
```

Usage

```scala mdoc
space.distance("the quick brown fox", "the quik brown fax")
```

Usage with spire's `distance` operator

Imports

```scala mdoc:silent
import axle.algebra.wrappedStringSpace
import spire.syntax.metricSpace.metricSpaceOps
```

Usage

```scala mdoc
"the quick brown fox" distance "the quik brown fax"

"the quick brown fox" distance "the quik brown fox"

"the quick brown fox" distance "the quick brown fox"
```
