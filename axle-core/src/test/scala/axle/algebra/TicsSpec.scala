package axle.algebra

import scala.Vector

import org.scalatest.funsuite._
import org.scalatest.matchers.should.Matchers

import cats.kernel.Eq
import cats.implicits._

class TicsSpec extends AnyFunSuite with Matchers {

  test("Tics[Double]") {

    val tics = Tics[Double].tics(0d, 1d).toVector

    // TODO: configurable precision
    val expected = Vector(
      (0.0, "0.0"),
      (0.1, "0.1"),
      (0.2, "0.2"),
      (0.3, "0.3"),
      (0.4, "0.4"),
      (0.5, "0.5"),
      (0.6, "0.6"),
      (0.7, "0.7"),
      (0.8, "0.8"),
      (0.9, "0.9"),
      (1.0, "1.0"))

    val vieq = Eq[Vector[(Double, String)]]

    // tics must be equalTo expected
    vieq.eqv(tics, expected) should be(true)
  }

  test("Tics[Rational]") {

    import spire.math.Rational

    val ts = Tics[Rational].tics(Rational(0), Rational(BigInt("84118943325460019771"), BigInt("4768371582031250000000")))

    ts should be(Vector((Rational(0), "0"), (Rational(1, 100), "1/100")))
  }

}
