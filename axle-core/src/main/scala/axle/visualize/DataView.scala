package axle.visualize

import axle.algebra.Plottable
import axle.algebra.Zero
import axle.stats.Probability
import scala.annotation.implicitNotFound
import cats.kernel.Order
import cats.Order.catsKernelOrderingForOrder

/**
 * implicits for Plot and BarChart
 *
 */

@implicitNotFound("Witness not found for DataView[${X}, ${Y}, ${D}]")
trait DataView[X, Y, D] {

  def keys(d: D): Traversable[X]

  def valueOf(d: D, x: X): Y

  def yRange(d: D): (Y, Y)
}

object DataView {

  final def apply[X, Y, D](implicit ev: DataView[X, Y, D]) = ev

  implicit def mapDataView[X: Order, Y: Plottable: Zero: Order]: DataView[X, Y, Map[X, Y]] =
    new DataView[X, Y, Map[X, Y]] {

      val yPlottable = Plottable[Y]
      val yZero = Zero[Y]

      def keys(d: Map[X, Y]): Traversable[X] = d.keys.toList.sorted

      def valueOf(d: Map[X, Y], x: X): Y = d.get(x).getOrElse(yZero.zero)

      def yRange(d: Map[X, Y]): (Y, Y) = {

        val yMin = (keys(d).map { x => valueOf(d, x) } ++ List(yZero.zero)).filter(yPlottable.isPlottable _).min
        val yMax = (keys(d).map { x => valueOf(d, x) } ++ List(yZero.zero)).filter(yPlottable.isPlottable _).max

        (yMin, yMax)
      }

    }

  implicit def probabilityDataView[X: Order, Y: Plottable: Zero: Order, D](
      implicit prob: Probability[D, X, Y]): DataView[X, Y, D] =
    new DataView[X, Y, D] {

      val yPlottable = Plottable[Y]
      val yZero = Zero[Y]

      def keys(d: D): Traversable[X] = prob.values(d)

      def valueOf(d: D, x: X): Y = prob.apply(d, x)

      def yRange(d: D): (Y, Y) = {

        val ks = keys(d)

        val yMin = (ks.map { x => valueOf(d, x) } ++ List(yZero.zero)).filter(yPlottable.isPlottable _).min
        val yMax = (ks.map { x => valueOf(d, x) } ++ List(yZero.zero)).filter(yPlottable.isPlottable _).max

        (yMin, yMax)
      }

    }

}
