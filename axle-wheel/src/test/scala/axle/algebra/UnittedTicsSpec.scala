package axle.algebra

import scala.Vector

import org.scalatest.funsuite._
import org.scalatest.matchers.should.Matchers

import edu.uci.ics.jung.graph.DirectedSparseGraph
import cats.kernel.Eq
import cats.implicits._
import spire.algebra._
import axle.quanta.Information
import axle.quanta.UnittedQuantity
import axle.jung.directedGraphJung

class UnittedTicsSpec extends AnyFunSuite with Matchers {

  implicit val mmd: MultiplicativeMonoid[Double] = spire.implicits.DoubleAlgebra

  test("Tics for UnittedQuantity") {

    implicit val id = {
      implicit val fieldDouble: Field[Double] = spire.implicits.DoubleAlgebra
      Information.converterGraphK2[Double, DirectedSparseGraph]
    }

    import id.bit
    implicit val base = bit

    val ticker = {
      implicit val fieldDouble: Field[Double] = spire.implicits.DoubleAlgebra
      axle.quanta.unittedTicsGraphK2[Information, Double, DirectedSparseGraph]
    }

    val tics = ticker.tics(0d *: bit, 1d *: bit).toVector

    // TODO: configurable precision
    val expected = Vector(
      (0.0 *: bit, "0.0"),
      (0.1 *: bit, "0.1"),
      (0.2 *: bit, "0.2"),
      (0.3 *: bit, "0.3"),
      (0.4 *: bit, "0.4"),
      (0.5 *: bit, "0.5"),
      (0.6 *: bit, "0.6"),
      (0.7 *: bit, "0.7"),
      (0.8 *: bit, "0.8"),
      (0.9 *: bit, "0.9"),
      (1.0 *: bit, "1.0"))

    val vieq = Eq[Vector[(UnittedQuantity[Information, Double], String)]]

    // tics must be equalTo expected
    vieq.eqv(tics, expected) should be(true)
  }

}
