package axle

import org.scalatest._

import edu.uci.ics.jung.graph.DirectedSparseGraph

import cats.implicits._

import spire.math.Rational
import spire.algebra._

import axle.stats.ConditionalProbabilityTable
import axle.stats.entropy
import axle.data.Coin
import axle.quanta.Information
import axle.jung.directedGraphJung

class InformationTheorySpec extends FunSuite with Matchers {

  implicit val fieldDouble: Field[Double] = spire.implicits.DoubleAlgebra

  test("hard-coded distributions") {

    implicit val id = Information.converterGraphK2[Double, DirectedSparseGraph]

    val d =
      ConditionalProbabilityTable(Map(
        "A" -> Rational(2, 10),
        "B" -> Rational(1, 10),
        "C" -> Rational(7, 10)))

    val e = entropy[String, Rational](d)

    e.magnitude should ===(1.1567796494470395)
  }

  test("coins") {

    val biasedCoin = Coin.flipModel(Rational(9, 10))
    val fairCoin = Coin.flipModel()

    implicit val id = Information.converterGraphK2[Double, DirectedSparseGraph]

    // assumes entropy is in bits
    val biasedCoinEntropy = entropy[Symbol, Rational](biasedCoin)
    biasedCoinEntropy.magnitude should be(0.4689955935892812)

    val fairCoinEntropy = entropy[Symbol, Rational](fairCoin)
    fairCoinEntropy.magnitude should be(1d)
  }

}
