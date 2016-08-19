
Road Map
========

## 0.2.0 (August 12, 2015)
* reorganize to minimize dependencies from axle-core, with witnesses in the axle-X jars (axle.X package) for library X
* LinearAlgebra typeclass
* Functor, Aggregatable typeclasses
* Show, Draw, Play typeclasses
* MAP@k, harmonicMean
* axle-spark
* Apache 2.0 license

## 0.2.x
* Increase test coverage
* Logistic Regression
* Akka Streams for animating visualizations
* Stochastic Lambda Calculus
* P() backed by BayesianNetwork (and Interaction graph, Elimination graph, Jointree)
* Neural Networks
* Hidden Markov Models
* Game Theory: information sets, equilibria
* Shapeless2 HLists for feature vectors
* netlib-java Matrix
* redo axle-game "family polymorphism"
* Algebird/Scalding for distributed matrices
* Performance benchmarking
* Physics (eg, how Volume relates to Flow)
* Use Shapeless for compound Quanta and Bayesian Networks
* A* Search
* HyperLogLog
* Decision Trees
* do-calculus (Causality)
* Heterogenous Model types
* Information Theory
* GPU/CUDA support

Previous Milestones
===================

## 0.1.M1 (July 15, 2012)
* Jblas-backed Matrix
* Jung-backed Graph
* Quanta (units of measurement)
* Linear Regression
* K-means

## 0.1.M2 (October 24, 2012)
* Genetic Algorithms
* Bug: x and y axis outside of plot area
* Naive Bayes
* show() in axle.visualize
* PCA
* Immutable matrices
* Optimize Plot of axle.quanta

## 0.1-M3 (December 11, 2012)
* Immutable graphs

## 0.1-M4 (December 16, 2013)
* Clean up axle.graph by scrapping attempt at family polymorphism
* Generalize InfoPlottable to QuantaPlottable

## 0.1-M5 (January 1, 2013)
* Bar Chart
* Minimax
* Texas Hold Em Poker

## 0.1-M6 (February 13, 2013)
* Initial version of <code>axle.algebra</code>
* No mutable state (except for permutations, combinations, and mutable buffer enrichment)
* <code>axle.quanta</code> conversion graph edges as functions
* Redoing JblasMatrixFactory as JblasMatrixModule (preparing for "cake" pattern")

## 0.1-M7 (February 19, 2013)
* Use <code>spire.math.Number</code> in <code>axle.quanta</code>
* Use <code>spire.algebra.MetricSpace</code> for <code>axle.lx.*VectorSpace</code> and <code>axle.ml.distance.*</code>

## 0.1-M8 (March 11, 2013)
* Akka for streaming data updates to Plot and Chart
* Tartarus English stemmer
* Create <code>axle.nlp</code> package and move much of <code>axle.lx</code> there
* Move Bayesian Networks code to <code>axle.pgm</code>
* <code>axle.actor</code> for Akka-related code

## 0.1-M9 (April 7, 2013)
* DNA sequence alignment algorithms in <code>axle.bio</code>
* <code>axle.logic</code>
* multi-project build, rename axle to axle-core, and split out axle-visualize

## 0.1-M10 (May 14, 2013)
* bug fixes in cards and poker
* api changes and bug fixes to visualizations required by hammer
* upgrade to akka 2.2-M3 and spire 0.4.0

## 0.1-M11 (February 26, 2014)
* REPL
* 3d visualizations using OpenGL (via jogl)
* More prevalent use of Spire typeclasses and number types

## 0.1-M12 (June 26, 2014)
* Upgrade to Scala 2.11.1
* Field context bound for classes in axle.stats and pgm
* axle.quanta conversions as Rational

## 0.1.13 through 0.1.17 (October 12, 2014)
* Distribution as a Monad
* Spire 'Module' for axle.quanta