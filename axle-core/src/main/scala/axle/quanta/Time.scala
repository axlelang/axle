package axle.quanta

import spire.algebra.Field
import spire.algebra.CModule
import cats.kernel.Eq
import axle.algebra.Bijection
import axle.algebra.DirectedGraph
import axle.algebra.Scale
import axle.algebra.Scale10s

case class Time() extends Quantum {

  def wikipediaUrl: String = "http://en.wikipedia.org/wiki/Orders_of_magnitude_(time)"

}

trait TimeUnits extends QuantumUnits[Time] {

  lazy val second = unit("second", "s", Some("http://en.wikipedia.org/wiki/Second"))
  lazy val s = second
  lazy val millisecond = unit("millisecond", "ms", Some("http://en.wikipedia.org/wiki/Millisecond"))
  lazy val ms = millisecond
  lazy val microsecond = unit("microsecond", "μs", Some("http://en.wikipedia.org/wiki/Microsecond"))
  lazy val μs = microsecond
  lazy val nanosecond = unit("nanosecond", "ns", Some("http://en.wikipedia.org/wiki/Nanosecond"))
  lazy val ns = nanosecond
  lazy val picosecond = unit("picosecond", "ps", Some("http://en.wikipedia.org/wiki/Picosecond"))
  lazy val ps = picosecond
  lazy val femtosecond = unit("femtosecond", "fs", Some("http://en.wikipedia.org/wiki/Femtosecond"))
  lazy val fs = femtosecond
  lazy val attosecond = unit("attosecond", "as", Some("http://en.wikipedia.org/wiki/Attosecond"))
  lazy val as = attosecond
  lazy val zeptosecond = unit("zeptosecond", "zs", Some("http://en.wikipedia.org/wiki/Zeptosecond"))
  lazy val zs = zeptosecond
  lazy val yoctosecond = unit("yoctosecond", "ys", Some("http://en.wikipedia.org/wiki/Yoctosecond"))
  lazy val ys = yoctosecond
  lazy val minute = unit("minute", "m", Some("http://en.wikipedia.org/wiki/Minute"))
  lazy val m = minute
  lazy val ky = millenium
  lazy val hour = unit("hour", "hr", Some("http://en.wikipedia.org/wiki/Hour"))
  lazy val day = unit("day", "d", Some("http://en.wikipedia.org/wiki/Day"))
  lazy val year = unit("year", "yr", Some("http://en.wikipedia.org/wiki/Year"))
  lazy val century = unit("century", "century", Some("http://en.wikipedia.org/wiki/Century"))
  lazy val millenium = unit("millenium", "ky", Some("http://en.wikipedia.org/wiki/Millenium"))
  lazy val megayear = unit("megayear", "my")
  lazy val my = megayear
  lazy val gigayear = unit("gigayear", "gy")
  lazy val gy = gigayear

  def units: List[UnitOfMeasurement[Time]] =
    List(second, millisecond, microsecond, nanosecond, picosecond, femtosecond, attosecond,
      zeptosecond, yoctosecond, minute, hour, day, year, century, millenium, megayear, gigayear)

}

trait TimeConverter[N] extends UnitConverter[Time, N] with TimeUnits {

  def defaultUnit = second
}

object Time {

  import spire.math._

  def converterGraphK2[N: Field: Eq: ConvertableTo, DG[_, _]](
    implicit
    moduleRational: CModule[N, Rational],
    evDG: DirectedGraph[DG[UnitOfMeasurement[Time], N => N], UnitOfMeasurement[Time], N => N]) =
    converterGraph[N, DG[UnitOfMeasurement[Time], N => N]]

  def converterGraph[N: Field: ConvertableTo: Eq, DG](
    implicit
    moduleRational: CModule[N, Rational],
    evDG: DirectedGraph[DG, UnitOfMeasurement[Time], N => N]) =
    new UnitConverterGraph[Time, N, DG] with TimeConverter[N] {

      def links: Seq[(UnitOfMeasurement[Time], UnitOfMeasurement[Time], Bijection[N, N])] =
        List[(UnitOfMeasurement[Time], UnitOfMeasurement[Time], Bijection[N, N])](
          (ms, s, Scale10s(3)),
          (μs, s, Scale10s(6)),
          (ns, s, Scale10s(9)),
          (ps, s, Scale10s(12)),
          (fs, s, Scale10s(15)),
          (as, s, Scale10s(18)),
          (zs, s, Scale10s(21)),
          (ys, s, Scale10s(24)),
          (s, m, Scale(Rational(60))),
          (m, hour, Scale(Rational(60))),
          (hour, day, Scale(Rational(24))),
          (day, year, Scale(365.25)),
          (year, century, Scale10s(2)),
          (year, ky, Scale10s(3)),
          (year, my, Scale10s(6)),
          (year, gy, Scale10s(9)))

    }

}
