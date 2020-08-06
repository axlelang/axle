package axle.pgm

import cats.kernel.Eq
import cats.kernel.Order
import cats.implicits._

import spire.algebra.Field
import spire.algebra.Ring
// import spire.implicits.multiplicativeSemigroupOps
import spire.implicits.additiveSemigroupOps
import spire.random.Dist
import spire.random.Generator

import axle.dummy
// import axle.algebra.RegionEq
import axle.algebra.Region
//import axle.algebra.RegionEmpty
//import axle.algebra.RegionAll
import axle.stats.Variable
import axle.stats.Factor
import axle.stats.ProbabilityModel
//import axle.math.Π
//import axle.math.Σ
import axle.syntax.directedgraph._

/**
 * The role of MonotypeBayesanNetwork is to add an additional type, C,
 * that represents the composite type of all Variables.
 * This allows a ProbabilityModel typeclass witness to be defined.
 * 
 */

class MonotypeBayesanNetwork[C, S, X, DG[_, _]](
  val bayesianNetwork: BayesianNetwork[S, X, DG[BayesianNetworkNode[S, X], Edge]],
  val select: (Variable[S], C) => S,
  val combine1: Vector[S] => C,
  val combine2: (Map[Variable[S], S]) => C)
  
object MonotypeBayesanNetwork {

  implicit def probabilityModelForMonotypeBayesanNetwork[I: Eq, DG[_, _]]: ProbabilityModel[({ type L[C, W] = MonotypeBayesanNetwork[C, I, W, DG] })#L] =
    new ProbabilityModel[({ type L[C, W] = MonotypeBayesanNetwork[C, I, W, DG] })#L] {

      def observeNodeAndAncestors[A, V: Dist: Ring: Order](
        model: MonotypeBayesanNetwork[A, I, V, DG],
        variable: Variable[I],
        acc: Map[Variable[I], I])(
        gen: Generator): Map[Variable[I], I] =
        if( acc.contains(variable) ) {
          acc
        } else {
          val ancestorMap: Map[Variable[I], I] = {
            val bnnForVariable = model.bayesianNetwork.bnnByVariable(variable)
            import model.bayesianNetwork.dg
            val parents: List[Variable[I]] = model.bayesianNetwork.graph.predecessors(bnnForVariable).toList.map(_.variable)
            parents.foldLeft(acc)({ case (acc, p) => observeNodeAndAncestors(model, p, acc)(gen) })
          }
          val i: I = {
            val factor: Factor[I, V] = model.bayesianNetwork.variableFactorMap(variable)
            val index: Int = factor.variablesWithValues.indexWhere(_._1 === variable)
            val p: Map[I, V] = factor.cases.filter { reqs =>
              factor.variablesWithValues.map(_._1).zip(reqs.map(_.x)).forall { case (v, value) =>
                (v === variable) || (ancestorMap(v) === value)
              }
            } map { reqs =>
              reqs(index).x -> factor.probabilities(reqs.toVector)
            } toMap
            val bars: Vector[(I, V)] = p.toVector.scanLeft((dummy[I], Ring[V].zero))((x, y) => (y._1, x._2 + y._2)).drop(1)
            val r: V = gen.next[V]
            bars.find({ case (_, v) => Order[V].gteqv(v, r) }).get._1 
          }
          ancestorMap + (variable -> i)
        }

      def observe[A, V: Dist: Ring: Order](model: MonotypeBayesanNetwork[A, I, V, DG])(gen: Generator): A = {
        import model.bayesianNetwork.dg
        val parents: List[Variable[I]] = model.bayesianNetwork.graph.vertices.toList.map(_.variable)
        val acc = Map.empty[Variable[I], I]
        val assignments = parents.foldLeft(acc)({ case (acc, p) =>
          observeNodeAndAncestors(model, p, acc)(gen)
        })
        model.combine2(assignments)
      }

      def probabilityOf[A, V: Field](model: MonotypeBayesanNetwork[A,I,V,DG])(predicate: Region[A]): V =
        model
        .bayesianNetwork
        .jointProbabilityTable
        .probabilities
        .map({ case (vreq, p) => (model.combine1(vreq.map(_.x)), p)})
        .filter({ case (a, p) => predicate.apply(a) })
        .toVector
        .map(_._2)
        .reduce(Field[V].plus)

      def filter[A, V: Field](model: MonotypeBayesanNetwork[A,I,V,DG])(predicate: Region[A]): MonotypeBayesanNetwork[A,I,V,DG] = ???

      def unit[A: Eq, V: Ring](a: A): MonotypeBayesanNetwork[A,I,V,DG] = ???

      def flatMap[A, B: Eq, V](model: MonotypeBayesanNetwork[A,I,V,DG])(f: A => MonotypeBayesanNetwork[B,I,V,DG]): MonotypeBayesanNetwork[B,I,V,DG] = ???

      def map[A, B: Eq, V](model: MonotypeBayesanNetwork[A,I,V,DG])(f: A => B): MonotypeBayesanNetwork[B,I,V,DG] = ???

      def redistribute[A: Eq, V: Ring](model: MonotypeBayesanNetwork[A,I,V,DG])(from: A, to: A, mass: V): MonotypeBayesanNetwork[A,I,V,DG] = ???

   }
}