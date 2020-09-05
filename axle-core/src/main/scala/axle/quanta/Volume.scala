package axle.quanta

import cats.kernel.Eq
import spire.algebra.Field
import spire.math.ConvertableTo
import axle.algebra.Bijection
import axle.algebra.DirectedGraph
import axle.algebra.Scale10s
import axle.algebra.BijectiveIdentity

case class Volume() extends Quantum {

  def wikipediaUrl: String = "http://en.wikipedia.org/wiki/Volume"

}

trait VolumeUnits extends QuantumUnits[Volume] {

  lazy val m3 = unit("m3", "m3") // derive
  lazy val km3 = unit("km3", "km3") // derive
  lazy val cm3 = unit("cm3", "cm3") // derive
  lazy val liter = unit("liter", "L", Some("http://en.wikipedia.org/wiki/Liter"))
  lazy val L = liter
  lazy val ℓ = liter
  lazy val milliliter = unit("milliliter", "mL")

  def units: List[UnitOfMeasurement[Volume]] =
    List(m3, km3, cm3, liter, milliliter)

}

trait VolumeConverter[N] extends UnitConverter[Volume, N] with VolumeUnits {

  def defaultUnit = liter
}

object Volume {

  def converterGraphK2[N: Field: Eq: ConvertableTo, DG[_, _]](
    implicit
    evDG: DirectedGraph[DG[UnitOfMeasurement[Volume], N => N], UnitOfMeasurement[Volume], N => N]) =
    converterGraph[N, DG[UnitOfMeasurement[Volume], N => N]]

  def converterGraph[N: Field: Eq: ConvertableTo, DG](
    implicit
    evDG: DirectedGraph[DG, UnitOfMeasurement[Volume], N => N]) =
    new UnitConverterGraph[Volume, N, DG] with VolumeConverter[N] {

      def links: Seq[(UnitOfMeasurement[Volume], UnitOfMeasurement[Volume], Bijection[N, N])] =
        List[(UnitOfMeasurement[Volume], UnitOfMeasurement[Volume], Bijection[N, N])](
          (milliliter, liter, Scale10s(3)),
          (cm3, milliliter, BijectiveIdentity[N]()))

    }
}
