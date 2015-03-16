package axle.quanta

import axle.algebra.Bijection
import axle.algebra.DirectedGraph
import spire.algebra.Eq
import spire.algebra.Field

case class Distance() extends Quantum {

  def wikipediaUrl: String = "http://en.wikipedia.org/wiki/Orders_of_magnitude_(length)"

}

trait DistanceUnits extends QuantumUnits[Distance] {

  lazy val foot = unit("foot", "ft")
  lazy val ft = foot
  lazy val mile = unit("mile", "m", Some("http://en.wikipedia.org/wiki/Mile"))
  lazy val meter = unit("meter", "m")
  lazy val kilometer = unit("kilometer", "km")
  lazy val km = kilometer
  lazy val centimeter = unit("centimeter", "cm")
  lazy val cm = centimeter
  lazy val millimeter = unit("millimeter", "mm")
  lazy val mm = millimeter
  lazy val micrometer = unit("micrometer", "μm")
  lazy val μm = micrometer
  lazy val nanometer = unit("nanometer", "nm")
  lazy val nm = nanometer
  lazy val astronomicalUnit = unit("Astronomical Unit", "AU", Some("http://en.wikipedia.org/wiki/Astronomical_unit"))
  lazy val au = astronomicalUnit
  lazy val astronomicalUnitSI = unit("Astronomical Unit (SI)", "AU", Some("http://en.wikipedia.org/wiki/Astronomical_unit"))
  lazy val auSI = astronomicalUnitSI
  lazy val lightyear = unit("lightyear", "ly", Some("http://en.wikipedia.org/wiki/Light-year"))
  lazy val ly = lightyear
  lazy val parsec = unit("parsec", "pc", Some("http://en.wikipedia.org/wiki/Parsec"))

  def units: List[UnitOfMeasurement[Distance]] =
    List(foot, mile, meter, kilometer, centimeter, millimeter, micrometer, nanometer,
      astronomicalUnit, astronomicalUnitSI, lightyear, parsec)

}

trait DistanceConverter[N] extends UnitConverter[Distance, N] with DistanceUnits

object Distance {

  def converterGraph[N: Field: Eq, DG[_, _]: DirectedGraph] =
    new UnitConverterGraph[Distance, N, DG] with DistanceConverter[N] {

      def links: Seq[(UnitOfMeasurement[Distance], UnitOfMeasurement[Distance], Bijection[N, N])] =
        List[(UnitOfMeasurement[Distance], UnitOfMeasurement[Distance], Bijection[N, N])](
          (foot, mile, ScaleInt(5280)),
          (foot, meter, ScaleDouble(3.2808398950131235)),
          (kilometer, mile, ScaleDouble(1.609344)),
          (lightyear, parsec, ScaleDouble(3.26)),
          (nm, meter, Scale10s(9)),
          (μm, meter, Scale10s(6)),
          (millimeter, meter, Scale10s(3)),
          (centimeter, meter, Scale10s(2)),
          (meter, kilometer, Scale10s(3)),
          (mile, au, ScaleDouble(92955807.3)),
          (km, auSI, ScaleDouble(149597870.7)),
          (km, ly, ScaleDouble(9460730472580.8)))

    }

}