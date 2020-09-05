package axle.bio

import scala.Vector

import cats.Functor
import cats.kernel.Order
import cats.kernel.Eq
import cats.implicits._

import spire.algebra.AdditiveMonoid
import spire.algebra.Ring
import spire.algebra.CModule
import spire.implicits.additiveGroupOps
import spire.implicits.additiveSemigroupOps

import axle.algebra.Aggregatable
import axle.algebra.Finite
import axle.algebra.FromStream
import axle.algebra.Indexed
import axle.algebra.LinearAlgebra
import axle.algebra.Zipper
import axle.algebra.SimilaritySpace
import axle.math._
import axle.syntax.finite.finiteOps
import axle.syntax.indexed.indexedOps
import axle.syntax.linearalgebra.matrixOps

import NeedlemanWunsch.computeF

/**
 *
 * http://en.wikipedia.org/wiki/Needleman-Wunsch_algorithm
 *
 */

object NeedlemanWunsch {

  def alignmentScoreK1[C[_], N: Eq, I: Ring: Eq, M, V: AdditiveMonoid: Eq](
    a:          C[N],
    b:          C[N],
    gap:        N,
    similarity: (N, N) => V,
    gapPenalty: V)(
    implicit
    finite:  Finite[C, I],
    zipper:  Zipper[C],
    functor: Functor[C],
    agg:     Aggregatable[C]): V =
    alignmentScore(a, b, gap, similarity, gapPenalty)

  /**
   * Computes the alignment score
   *
   * Arguments A and B must be of the same length
   *
   * alignmentScore("AGACTAGTTAC", "CGA---GACGT")
   *
   * ←
   */

  def alignmentScore[S[_], N: Eq, I: Ring: Eq, M, V: AdditiveMonoid: Eq](
    A:          S[N],
    B:          S[N],
    gap:        N,
    similarity: (N, N) => V,
    gapPenalty: V)(
    implicit
    finite:  Finite[S, I],
    zipper:  Zipper[S],
    functor: Functor[S],
    agg:     Aggregatable[S]): V = {

    assert(A.size === B.size)

    val zipped = zipper.zip(A, B)

    val scores: S[V] =
      zipped.map({ ab =>
        val an = ab._1.asInstanceOf[N]
        val bn = ab._2.asInstanceOf[N]
        if (an === gap || bn === gap) { gapPenalty } else { similarity(an, bn) }
      })

    Σ[V, S](scores)
  }

  /**
   *
   * Computes the "F" matrix for two nucleotide sequences, A and B
   *
   */

  def computeF[I: Ring, S[_], N, M, V: AdditiveMonoid: Order](
    A:          S[N],
    B:          S[N],
    similarity: (N, N) => V,
    gapPenalty: V)(
    implicit
    la:      LinearAlgebra[M, I, I, V],
    indexed: Indexed[S, I],
    finite:  Finite[S, I],
    module:  CModule[V, I]): M = {

    val one = Ring[I].one

    la.matrix(
      A.size + one,
      B.size + one,
      implicitly[AdditiveMonoid[V]].zero,
      (i: I) => module.timesr(gapPenalty, i),
      (j: I) => module.timesr(gapPenalty, j),
      (i: I, j: I, aboveleft: V, left: V, above: V) => {
        Vector(
          aboveleft + similarity(A.at(i - one), B.at(j - one)),
          above + gapPenalty,
          left + gapPenalty).max
      })

  }

  def alignStep[S[_], N: Eq, M, I: Ring: Order, V: AdditiveMonoid: Eq](
    i:          I,
    j:          I,
    A:          S[N],
    B:          S[N],
    F:          M,
    similarity: (N, N) => V,
    gap:        N,
    gapPenalty: V)(
    implicit
    la:      LinearAlgebra[M, I, I, V],
    indexed: Indexed[S, I]): (N, N, I, I) = {

    val one = Ring[I].one
    val zero = Ring[I].zero

    if ((i > zero) && (j > zero) && (F.get(i, j) === (F.get(i - one, j - one) + similarity(A.at(i - one), B.at(j - one))))) {
      (A.at(i - one), B.at(j - one), i - one, j - one)
    } else if (i > zero && F.get(i, j) === (F.get(i - one, j) + gapPenalty)) {
      (A.at(i - one), gap, i - one, j)
    } else {
      assert(j > zero && F.get(i, j) === F.get(i, j - one) + gapPenalty)
      (gap, B.at(j - one), i, j - one)
    }
  }

  def _optimalAlignment[S[_], N: Eq, M, I: Ring: Order, V: AdditiveMonoid: Eq](
    i:          I,
    j:          I,
    A:          S[N],
    B:          S[N],
    similarity: (N, N) => V,
    gap:        N,
    gapPenalty: V,
    F:          M)(
    implicit
    la:      LinearAlgebra[M, I, I, V],
    indexed: Indexed[S, I]): LazyList[(N, N)] = {

    val zero = Ring[I].zero

    if ((i > zero) || (j > zero)) {
      val (preA, preB, newI, newJ) = alignStep(i, j, A, B, F, similarity, gap, gapPenalty)
      LazyList.cons((preA, preB), _optimalAlignment(newI, newJ, A, B, similarity, gap, gapPenalty, F))
    } else {
      LazyList.empty
    }
  }

  def optimalAlignment[S[N], N: Eq, M, I: Ring: Order, V: AdditiveMonoid: Order: Eq](
    A:          S[N],
    B:          S[N],
    similarity: (N, N) => V,
    gap:        N,
    gapPenalty: V)(
    implicit
    la:      LinearAlgebra[M, I, I, V],
    indexed: Indexed[S, I],
    finite:  Finite[S, I],
    fs:      FromStream[S[N], N],
    module:  CModule[V, I]): (S[N], S[N]) = {

    val F = computeF(A, B, similarity, gapPenalty)

    val (alignA, alignB) = _optimalAlignment(
      A.size,
      B.size,
      A,
      B,
      similarity,
      gap,
      gapPenalty,
      F).reverse.unzip

    (fs.fromStream(alignA), fs.fromStream(alignB))
  }

}

case class NeedlemanWunschSimilaritySpace[S[_], N: Eq, M, I: Ring: Order, V: AdditiveMonoid: Order](
  baseSimilarity: (N, N) => V,
  gapPenalty: V)(
  implicit
  la:      LinearAlgebra[M, I, I, V],
  indexed: Indexed[S, I],
  finite:  Finite[S, I],
  module:  CModule[V, I])
  extends SimilaritySpace[S[N], V] {

  def similarity(s1: S[N], s2: S[N]): V =
    computeF(s1, s2, baseSimilarity, gapPenalty).get(s1.size, s2.size)

}
