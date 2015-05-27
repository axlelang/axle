package axle.bio

import scala.Vector
import scala.collection.immutable.Stream
import scala.collection.immutable.Stream.cons
import scala.collection.immutable.Stream.empty
import scala.reflect.ClassTag

import axle.algebra.LinearAlgebra
import axle.algebra.Finite
import axle.algebra.FromStream
import axle.algebra.Indexed

import spire.algebra.Eq
import spire.algebra.Group
import spire.algebra.MetricSpace
import spire.algebra.Order
import spire.algebra.Ring

import spire.compat.ordering
import spire.implicits.additiveGroupOps
import spire.implicits.additiveSemigroupOps
import spire.implicits.eqOps
import spire.implicits.moduleOps
import spire.implicits.partialOrderOps

import axle.syntax.finite.finiteOps
import axle.syntax.functor.functorOps
import axle.syntax.indexed.indexedOps
import axle.syntax.linearalgebra.matrix
import axle.syntax.linearalgebra.matrixOps

/**
 *
 * http://en.wikipedia.org/wiki/Smith-Waterman_algorithm
 *
 */

object SmithWaterman {

  object Default {

    def w(x: Char, y: Char, mismatchPenalty: Int): Int =
      if (x != y) {
        mismatchPenalty
      } else {
        2 // also see NeedlemanWunsch.Default.similarity
      }

    val mismatchPenalty = -1

    val gap = '-'

  }

  /**
   *
   * Computes the "H" matrix for two DNA sequences, A and B
   *
   * Same as Needleman-Wunsch's F matrix, except that all entries
   * in the matrix are non-negative.
   *
   */

  def computeH[S[_], C: ClassTag, M, I: Ring, V: Ring: Order](
    A: S[C],
    B: S[C],
    w: (C, C, V) => V,
    mismatchPenalty: V)(
      implicit la: LinearAlgebra[M, I, I, V],
      indexed: Indexed[S, I],
      finite: Finite[S, I]): M = {

    val iOne = Ring[I].one
    val vZero = Ring[V].zero

    la.matrix(
      A.size + iOne,
      B.size + iOne,
      vZero,
      (i: I) => vZero,
      (j: I) => vZero,
      (i: I, j: I, aboveleft: V, left: V, above: V) => Vector(
        vZero,
        aboveleft + w(A.at(i - iOne), B.at(j - iOne), mismatchPenalty),
        above + mismatchPenalty,
        left + mismatchPenalty).max)
  }

  def alignStep[S[_], C: ClassTag, M, I: Ring: Order, V: Ring: Order: Eq](
    i: I,
    j: I,
    A: S[C],
    B: S[C],
    w: (C, C, V) => V,
    H: M,
    mismatchPenalty: V,
    gap: C)(
      implicit la: LinearAlgebra[M, I, I, V],
      indexed: Indexed[S, I]): (C, C, I, I) = {

    val iZero = Ring[I].zero
    val iOne = Ring[I].one

    if (i > iZero && j > iZero && (H.get(i, j) === H.get(i - iOne, j - iOne) + w(A.at(i - iOne), B.at(j - iOne), mismatchPenalty))) {
      (A.at(i - iOne), B.at(j - iOne), i - iOne, j - iOne)
    } else if (i > 0 && H.get(i, j) === H.get(i - iOne, j) + mismatchPenalty) {
      (A.at(i - iOne), gap, i - iOne, j)
    } else {
      assert(j > 0 && H.get(i, j) === H.get(i, j - iOne) + mismatchPenalty)
      (gap, B.at(j - iOne), i, j - iOne)
    }
  }

  def _optimalAlignment[S[_], C: ClassTag, M, I: Ring: Order, V: Ring: Order](
    i: I,
    j: I,
    A: S[C],
    B: S[C],
    w: (C, C, V) => V,
    mismatchPenalty: V,
    gap: C,
    H: M)(
      implicit la: LinearAlgebra[M, I, I, V],
      indexed: Indexed[S, I]): Stream[(C, C)] =
    if (i > 0 || j > 0) {
      val (preA, preB, newI, newJ) = alignStep[S, C, M, I, V](i, j, A, B, w, H, mismatchPenalty, gap)
      cons((preA, preB), _optimalAlignment[S, C, M, I, V](newI, newJ, A, B, w, mismatchPenalty, gap, H))
    } else {
      empty
    }

  def optimalAlignment[S[_], C: ClassTag, M, I: Ring: Order, V: Ring: Order](
    A: S[C],
    B: S[C],
    w: (C, C, V) => V,
    mismatchPenalty: V,
    gap: C)(
      implicit la: LinearAlgebra[M, I, I, V],
      indexed: Indexed[S, I],
      finite: Finite[S, I],
      fs: FromStream[S]): (S[C], S[C]) = {

    val H = computeH[S, C, M, I, V](A, B, w, mismatchPenalty)

    val (alignmentA, alignmentB) = _optimalAlignment[S, C, M, I, V](A.size, B.size, A, B, w, mismatchPenalty, gap, H).unzip

    (fs.fromStream(alignmentA.reverse), fs.fromStream(alignmentB.reverse))
  }

}

case class SmithWatermanMetricSpace[S[_], C: ClassTag, M, I: Ring, V: Ring: Order](
    w: (C, C, V) => V,
    mismatchPenalty: V)(
        implicit la: LinearAlgebra[M, I, I, V],
        finite: Finite[S, I],
        indexed: Indexed[S, I]) extends MetricSpace[S[C], V] {

  def distance(s1: S[C], s2: S[C]): V = {

    val H = SmithWaterman.computeH[S, C, M, I, V](s1, s2, w, mismatchPenalty)

    H.get(s1.size, s2.size)
  }

}
