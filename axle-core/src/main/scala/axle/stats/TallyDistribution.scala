package axle.stats

import cats.Monad
import cats.Show
import cats.kernel.Order
import cats.implicits._

import spire.algebra.Field
import spire.algebra.Ring

import spire.implicits.multiplicativeGroupOps
import spire.implicits.multiplicativeSemigroupOps
import spire.random.Dist
import spire.random.Generator

import axle.math.Σ
import axle.dummy
import axle.algebra.Region
import axle.algebra.RegionEq
import axle.probability._

object TallyDistribution {

  implicit def show[A: Order: Show, V: Show: Field]: Show[TallyDistribution[A, V]] = td =>
    td.values.sorted.map(a => {
      val aString = Show[A].show(a)
      // (aString + (1 to (td.charWidth - aString.length)).map(i => " ").mkString("") + " " + string(td.probabilityOf(a)))
      (aString + " " + Show[V].show(TallyDistribution.kolmogorovWitness.probabilityOf(td)(RegionEq(a))))
    }).mkString("\n")

    implicit val kolmogorovWitness: Kolmogorov[TallyDistribution] =
      new Kolmogorov[TallyDistribution] {
        def probabilityOf[A, V](model: TallyDistribution[A, V])(predicate: Region[A])(implicit fieldV: Field[V]): V =
          Σ(model.values.filter(predicate).map(model.tally)) / model.totalCount
      }

    implicit val samplerWitness: Sampler[TallyDistribution] = 
      new Sampler[TallyDistribution] {
        def sample[A, V](model: TallyDistribution[A, V])(gen: Generator)(implicit spireDist: Dist[V], ringV: Ring[V], orderV: Order[V]): A = {
          val r: V = model.totalCount * gen.next[V]
          model.bars.find({ case (_, v) => orderV.gteqv(v, r) }).get._1 // or distribution is malformed
        }
      }

    implicit def monadWitness[V: Ring]: Monad[TallyDistribution[?, V]] =
      new Monad[TallyDistribution[?, V]] {
  
        def pure[A](a: A): TallyDistribution[A, V] =
          TallyDistribution(Map(a -> Ring[V].one))

        override def map[A, B](model: TallyDistribution[A, V])(f: A => B): TallyDistribution[B, V] =
          TallyDistribution[B, V](
            model.tally.map({ case (a, v) => f(a) -> v }) // TODO use eqA to unique
          )
  
        def flatMap[A, B](model: TallyDistribution[A, V])(f: A => TallyDistribution[B, V]): TallyDistribution[B, V] = {
          val p = model.values.toVector.flatMap { a =>
            val tallyA = model.tally.apply(a)
            val inner = f(a)
            inner.values.toVector.map { b =>
              b -> model.ringV.times(tallyA, inner.tally.apply(b))
            }
          }.groupBy(_._1).map({ case (b, bvs) => b -> bvs.map(_._2).reduce(model.ringV.plus)})
          TallyDistribution(p)
        }

        def tailRecM[A, B](a: A)(f: A => axle.stats.TallyDistribution[Either[A,B],V]): axle.stats.TallyDistribution[B,V] = ???

      }

  implicit val bayesWitness: Bayes[TallyDistribution] =
    new Bayes[TallyDistribution] {

      def filter[A, V](model: TallyDistribution[A, V])(predicate: Region[A])(implicit fieldV: Field[V]): TallyDistribution[A, V] = {
        val newMap: Map[A, V] = model.tally.toVector.filter({ case (a, v) => predicate(a)}).groupBy(_._1).map( bvs => bvs._1 -> Σ(bvs._2.map(_._2)) )
        val newDenominator: V = Σ(newMap.values)
        TallyDistribution[A, V](newMap.view.mapValues(v => v / newDenominator).toMap)
      }
    }

}
case class TallyDistribution[A, V](
  tally:    Map[A, V])(implicit val ringV: Ring[V]) {

  val values: IndexedSeq[A] = tally.keys.toVector

  val totalCount: V = Σ[V, Iterable](tally.values)

  val bars: Map[A, V] =
    tally.scanLeft((dummy[A], ringV.zero))((x, y) =>
     (y._1, ringV.plus(x._2, y._2))
    ).drop(1).toMap

}
