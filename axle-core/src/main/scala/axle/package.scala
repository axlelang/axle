
/**
 * Copyright (c) 2011-2014 Adam Pingel
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

import scala.collection.mutable.Buffer

import axle.EnrichedArray
import axle.EnrichedByteArray
import axle.EnrichedGenSeq
import axle.EnrichedGenTraversable
import axle.EnrichedIndexedSeq
import axle.EnrichedInt
import axle.EnrichedMutableBuffer
import axle.forall
import axle.thereexists
import axle.algebra.Aggregatable
import axle.algebra.DirectedGraph
import axle.algebra.Finite
import axle.algebra.Functor
import axle.algebra.Π
import spire.algebra.Bool
import spire.algebra.Eq
import spire.algebra.Field
import spire.algebra.NRoot
import spire.algebra.Order
import spire.algebra.AdditiveMonoid
import spire.algebra.Module
import spire.algebra.MultiplicativeMonoid
import spire.algebra.Ring
import spire.algebra.Trig
import spire.compat.ordering
import spire.implicits.eqOps
import spire.implicits.moduleOps
import spire.implicits.nrootOps
import spire.implicits.semiringOps
import spire.implicits.convertableOps
import spire.math.Rational
import axle.quanta.Angle
import axle.quanta.UnittedQuantity
import axle.quanta.UnitOfMeasurement
import axle.quanta.AngleConverter
import axle.quanta.Distance
import spire.math.ConvertableFrom
import spire.math.ConvertableTo
import scala.language.implicitConversions

/**
 *
 * See spire.optional.unicode.SymbolicSetOps for ∩ ∪ etc
 *
 */

package object axle {

  //  val Sigma = Σ _
  //
  //  val Pi = Π _

  val ∀ = forall

  val ∃ = thereexists

  /**
   * Englishman John Wallis (1616 - 1703) approximation of π in 1655
   *
   */
  def wallisΠ(iterations: Int = 10000) =
    2 * Π[Rational, IndexedSeq[Rational]]((1 to iterations) map { n => Rational((2 * n) * (2 * n), (2 * n - 1) * (2 * n + 1)) })

  /**
   * Monte Carlo approximation of pi http://en.wikipedia.org/wiki/Monte_Carlo_method
   *
   * TODO get n2v implicitly?
   *
   */

  def monteCarloPiEstimate[F, N, V: ConvertableTo, G](
    trials: F,
    n2v: N => V)(
      implicit finite: Finite[F, N],
      functor: Functor[F, N, V, G],
      agg: Aggregatable[G, V, V],
      field: Field[V]): V = {

    import spire.math.random
    import axle.algebra.Σ
    import axle.syntax.functor.functorOps
    import spire.implicits.multiplicativeSemigroupOps
    import spire.implicits.multiplicativeGroupOps

    val randomPointInCircle: () => V = () => {
      val x = random * 2 - 1
      val y = random * 2 - 1
      if (x * x + y * y < 1) field.one else field.zero
    }

    val vFour = ConvertableTo[V].fromDouble(4d)

    val counts: G = trials.map(i => randomPointInCircle())

    val s: V = Σ(counts)

    val numerator: V = vFour * s

    val denominator: V = n2v(finite.size(trials))

    numerator / denominator
  }

  def distanceOnSphere[N: MultiplicativeMonoid](
    angle: UnittedQuantity[Angle, N],
    sphereRadius: UnittedQuantity[Distance, N])(
      implicit angleConverter: AngleConverter[N],
      ctn: ConvertableTo[N],
      angleModule: Module[UnittedQuantity[Angle, N], N],
      distanceModule: Module[UnittedQuantity[Distance, N], N]): UnittedQuantity[Distance, N] =
    sphereRadius :* ((angle in angleConverter.radian).magnitude)

  def sine[N: MultiplicativeMonoid: Eq: Trig](
    a: UnittedQuantity[Angle, N])(
      implicit converter: AngleConverter[N]): N =
    spire.math.sin((a in converter.radian).magnitude)

  def cosine[N: MultiplicativeMonoid: Eq: Trig](
    a: UnittedQuantity[Angle, N])(
      implicit converter: AngleConverter[N]): N =
    spire.math.cos((a in converter.radian).magnitude)

  def tangent[N: MultiplicativeMonoid: Eq: Trig](
    a: UnittedQuantity[Angle, N])(
      implicit converter: AngleConverter[N]): N =
    spire.math.tan((a in converter.radian).magnitude)

  def arcTangent[N: Trig](x: N)(
    implicit converter: AngleConverter[N]): UnittedQuantity[Angle, N] =
    spire.math.atan(x) *: converter.radian

  def arcTangent2[N: Trig](x: N, y: N)(
    implicit converter: AngleConverter[N]): UnittedQuantity[Angle, N] =
    spire.math.atan2(x, y) *: converter.radian

  def arcCosine[N: Trig](x: N)(
    implicit converter: AngleConverter[N]): UnittedQuantity[Angle, N] =
    spire.math.acos(x) *: converter.radian

  def arcSine[N: Trig](x: N)(
    implicit converter: AngleConverter[N]): UnittedQuantity[Angle, N] =
    spire.math.asin(x) *: converter.radian

  implicit val orderSymbols: Order[Symbol] =
    new Order[Symbol] {
      def compare(x: Symbol, y: Symbol): Int = Order[String].compare(string(x), string(y))
    }

  implicit val orderStrings = Order.from((s1: String, s2: String) => s1.compare(s2))

  implicit val orderChars = Order.from((c1: Char, c2: Char) => c1.compare(c2))

  implicit val orderBooleans = Order.from((b1: Boolean, b2: Boolean) => b1.compare(b2))

  // See spire.syntax.Syntax DoubleOrder
  implicit val orderDoubles = Order.from((d1: Double, d2: Double) => d1.compare(d2))

  implicit def enrichGenSeq[T](genSeq: collection.GenSeq[T]): EnrichedGenSeq[T] = EnrichedGenSeq(genSeq)

  implicit def enrichGenTraversable[T: Manifest](gt: collection.GenTraversable[T]): EnrichedGenTraversable[T] = EnrichedGenTraversable(gt)

  implicit def enrichIndexedSeq[T: Manifest](is: IndexedSeq[T]): EnrichedIndexedSeq[T] = EnrichedIndexedSeq(is)

  implicit def enrichByteArray(barr: Array[Byte]): EnrichedByteArray = EnrichedByteArray(barr)

  implicit def enrichMutableBuffer[T](buffer: Buffer[T]): EnrichedMutableBuffer[T] = EnrichedMutableBuffer(buffer)

  implicit def enrichArray[T: Manifest](arr: Array[T]): EnrichedArray[T] = EnrichedArray(arr)

  implicit def enrichInt(n: Int): EnrichedInt = EnrichedInt(n)

  def fib(n: Int): Int = (1 to n).foldLeft((1, 1))((pre, i) => (pre._2, pre._1 + pre._2))._1

  def recfib(n: Int): Int = n match { case 0 | 1 => 1 case _ => recfib(n - 2) + recfib(n - 1) }

  /**
   * http://en.wikipedia.org/wiki/Ackermann_function
   */

  def ackermann(m: Long, n: Long): Long = {

    import spire.implicits.LongAlgebra

    if (m === 0L) {
      n + 1
    } else if (m > 0 && n === 0L) {
      ackermann(m - 1, 1)
    } else {
      ackermann(m - 1, ackermann(m, n - 1))
    }
  }

  /**
   * https://en.wikipedia.org/wiki/Logistic_map
   */

  def logisticMap[N: Ring](λ: N): N => N = {
    import spire.implicits.multiplicativeSemigroupOps
    import spire.implicits.additiveGroupOps
    x => λ * x * (Ring[N].one - x)
  }

  // Fundamental:

  def id[A](x: A): A = x

  // def argmax[K, N: Order](ks: Iterable[K], f: K => N): K = ks.map(k => (k, f(k))).maxBy(_._2)._1

  // IO

  def getLine(): String = scala.io.Source.stdin.getLines().next

  // List enrichments:

  def replicate[T](n: Int)(v: T): List[T] = (0 until n).map(i => v).toList

  def reverse[T](l: List[T]): List[T] = l.reverse

  def intersperse[T](d: T)(l: List[T]): List[T] =
    (0 until (2 * l.size - 1)).map(i => i % 2 match { case 0 => l(i / 2) case 1 => d }).toList

  // more math

  def square[N: Field](x: N): N = x ** 2

  def √[N: NRoot](x: N): N = x.sqrt

  implicit def eqSet[S: Eq]: Eq[Set[S]] = new Eq[Set[S]] {

    import spire.implicits.IntAlgebra

    def eqv(x: Set[S], y: Set[S]): Boolean = (x.size === y.size) && x.intersect(y).size === x.size
  }

  implicit def eqIndexedSeq[T: Eq]: Eq[IndexedSeq[T]] = new Eq[IndexedSeq[T]] {
    def eqv(x: IndexedSeq[T], y: IndexedSeq[T]): Boolean = {
      val lhs = (x.size == y.size)
      val rhs = (x.zip(y).forall({ case (a, b) => a === b }))
      lhs && rhs
    }
  }

  implicit def eqSeq[T: Eq]: Eq[Seq[T]] = new Eq[Seq[T]] {
    def eqv(x: Seq[T], y: Seq[T]): Boolean = {
      val lhs = (x.size == y.size)
      val rhs = (x.zip(y).forall({ case (a, b) => a === b }))
      lhs && rhs
    }
  }

  def string[T: Show](t: T): String = Show[T].text(t)

  def show[T: Show](t: T): Unit = println(string(t))

  def html[T: HtmlFrom](t: T): xml.Node = HtmlFrom[T].toHtml(t)

}
