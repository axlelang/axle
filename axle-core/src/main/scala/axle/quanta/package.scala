package axle

import cats.Show
import cats.kernel.Eq
import cats.kernel.Order
import cats.implicits._

import spire.algebra._
import spire.implicits.additiveGroupOps
import spire.implicits.additiveSemigroupOps
import spire.implicits.multiplicativeSemigroupOps
import spire.implicits.signedOps

import axle.algebra.LengthSpace
import axle.algebra.Plottable
import axle.algebra.Tics

package object quanta {

  implicit def quantumAdditiveGroup[Q, N: MultiplicativeMonoid](
    implicit
    converter:     UnitConverter[Q, N],
    additiveGroup: AdditiveGroup[N]): AdditiveGroup[UnittedQuantity[Q, N]] =
    new AdditiveGroup[UnittedQuantity[Q, N]] {

      // AdditiveGroup
      def negate(x: UnittedQuantity[Q, N]): UnittedQuantity[Q, N] =
        UnittedQuantity(-x.magnitude, x.unit)

      // AdditiveMonoid
      def zero: UnittedQuantity[Q, N] =
        UnittedQuantity(additiveGroup.zero, converter.defaultUnit)

      // AdditiveSemigroup
      def plus(x: UnittedQuantity[Q, N], y: UnittedQuantity[Q, N]): UnittedQuantity[Q, N] =
        UnittedQuantity((x in y.unit).magnitude + y.magnitude, y.unit)
    }

  implicit def modulize[N, Q](
    implicit
    fieldn:    Field[N],
    converter: UnitConverter[Q, N]): CModule[UnittedQuantity[Q, N], N] = {

    val additiveGroup = quantumAdditiveGroup[Q, N]

    new CModule[UnittedQuantity[Q, N], N] {

      def negate(x: UnittedQuantity[Q, N]): UnittedQuantity[Q, N] =
        additiveGroup.negate(x)

      def zero: UnittedQuantity[Q, N] =
        additiveGroup.zero

      def plus(x: UnittedQuantity[Q, N], y: UnittedQuantity[Q, N]): UnittedQuantity[Q, N] =
        additiveGroup.plus(x, y)

      implicit def scalar: CRing[N] = fieldn // Module

      def timesl(s: N, v: UnittedQuantity[Q, N]): UnittedQuantity[Q, N] =
        UnittedQuantity(s * v.magnitude, v.unit)
    }
  }

  implicit def uqPlottable[Q, N: Plottable]: Plottable[UnittedQuantity[Q, N]] =
    new Plottable[UnittedQuantity[Q, N]] {

      override def isPlottable(t: UnittedQuantity[Q, N]): Boolean = Plottable[N].isPlottable(t.magnitude)
    }

  implicit def unittedAdditiveMonoid[Q, N: AdditiveMonoid: MultiplicativeMonoid](
    implicit
    converter: UnitConverter[Q, N],
    base:      UnitOfMeasurement[Q]): AdditiveMonoid[UnittedQuantity[Q, N]] =
    new AdditiveMonoid[UnittedQuantity[Q, N]] {

      val am = implicitly[AdditiveMonoid[N]]

      def zero: UnittedQuantity[Q, N] = am.zero *: base

      // AdditiveSemigroup
      def plus(x: UnittedQuantity[Q, N], y: UnittedQuantity[Q, N]): UnittedQuantity[Q, N] =
        UnittedQuantity((converter.convert(x, y.unit)).magnitude + y.magnitude, y.unit)

    }

  def unittedTicsGraphK2[Q, N: Field: Eq: Tics: Show, DG[_, _]](
    implicit
    base:    UnitOfMeasurement[Q],
    convert: UnitConverter[Q, N]): Tics[UnittedQuantity[Q, N]] =
    unittedTics[Q, N, DG[UnitOfMeasurement[Q], N => N]]

  implicit def unittedTics[Q, N: Field: Eq: Tics: Show, DG](
    implicit
    base:    UnitOfMeasurement[Q],
    convert: UnitConverter[Q, N]): Tics[UnittedQuantity[Q, N]] =
    new Tics[UnittedQuantity[Q, N]] {

      def tics(from: UnittedQuantity[Q, N], to: UnittedQuantity[Q, N]): Seq[(UnittedQuantity[Q, N], String)] =
        Tics[N].tics((from in base).magnitude, (to in base).magnitude) map {
          case (v, label) => {
            val vu = UnittedQuantity[Q, N](v, base)
            (vu, v.show)
          }
        }
    }

  implicit def unittedLengthSpace[Q, N: Field: Order: Signed](
    implicit
    base:    UnitOfMeasurement[Q],
    space:   LengthSpace[N, Double, N],
    convert: UnitConverter[Q, N]) =
    new LengthSpace[UnittedQuantity[Q, N], UnittedQuantity[Q, N], N] {

      val field = Field[N]

      def distance(v: UnittedQuantity[Q, N], w: UnittedQuantity[Q, N]): UnittedQuantity[Q, N] = {
        val d: N = field.minus((v in base).magnitude, (w in base).magnitude)
        d.abs *: base
      }

      def onPath(left: UnittedQuantity[Q, N], right: UnittedQuantity[Q, N], p: N): UnittedQuantity[Q, N] =
        ((field.minus((right in base).magnitude, (left in base).magnitude)) * p + (left in base).magnitude) *: base

      def portion(left: UnittedQuantity[Q, N], v: UnittedQuantity[Q, N], right: UnittedQuantity[Q, N]): N =
        space.portion((left in base).magnitude, (v in base).magnitude, (right in base).magnitude)

    }

}
