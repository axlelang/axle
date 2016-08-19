
Graph
=====

DirectedGraph typeclass and witnesses for the Jung package

Directed Graph
--------------

Imports and implicits

```tut:silent
import axle._
import axle.algebra._
import axle.jung._
import axle.syntax.directedgraph.directedGraphOps
import axle.syntax.undirectedgraph.undirectedGraphOps
import spire.implicits.StringOrder
import spire.implicits.eqOps
import axle.syntax.finite.finiteOps
import edu.uci.ics.jung.graph.DirectedSparseGraph

class Edge
implicit val showEdge: Show[Edge] = new Show[Edge] { def text(e: Edge): String = "" }
```

Example

```tut
val jdg = DirectedGraph.k2[DirectedSparseGraph, String, Edge]

val a = "a"
val b = "b"
val c = "c"
val d = "d"

val dg = jdg.make(List(a, b, c, d),
  List(
    (a, b, new Edge),
    (b, c, new Edge),
    (c, d, new Edge),
    (d, a, new Edge),
    (a, c, new Edge),
    (b, d, new Edge)))
```

```tut
dg.size
dg.findVertex(_ === "a").map(v => dg.successors(v))
dg.findVertex(_ === "c").map(v => dg.successors(v))
dg.findVertex(_ === "c").map(v => dg.predecessors(v))
dg.findVertex(_ === "c").map(v => dg.neighbors(v))
```

Visualize the graph

```tut
import axle.web._

svg(dg, "SimpleDirectedGraph.svg")
```

![directed graph](../images/SimpleDirectedGraph.svg)

Undirected Graph
----------------

Imports and implicits

```tut:silent
import edu.uci.ics.jung.graph.UndirectedSparseGraph

class Edge
implicit val showEdge: Show[Edge] = new Show[Edge] { def text(e: Edge): String = "" }
```

Example

```tut
val jug = UndirectedGraph.k2[UndirectedSparseGraph, String, Edge]

val a = "a"
val b = "b"
val c = "c"
val d = "d"

val ug = jug.make(List(a, b, c, d),
  List(
    (a, b, new Edge),
    (b, c, new Edge),
    (c, d, new Edge),
    (d, a, new Edge),
    (a, c, new Edge),
    (b, d, new Edge)))
```

```tut
ug.size

ug.findVertex(_ == "c").map(v => ug.neighbors(v))

ug.findVertex(_ == "a").map(v => ug.neighbors(v))
```

Visualize the graph

```tut
import axle.web._

svg(ug, "SimpleUndirectedGraph.svg")
```

![undirected graph](../images/SimpleUndirectedGraph.svg)