package axle.lx

import org.scalatest.funsuite._
import org.scalatest.matchers.should.Matchers

import cats.implicits._

import axle.algebra._
import GoldParadigm._

class GoldSpecification extends AnyFunSuite with Matchers {

  val mHi = Morpheme("hi")
  val mIm = Morpheme("I'm")
  val mYour = Morpheme("your")
  val mMother = Morpheme("Mother")
  val mShut = Morpheme("shut")
  val mUp = Morpheme("up")

  val Σ = Vocabulary(Set(mHi, mIm, mYour, mMother, mShut, mUp))

  val s1 = Expression(mHi :: mIm :: mYour :: mMother :: Nil)
  val s2 = Expression(mShut :: mUp :: Nil)

  val ℒ = Language(Set(s1, s2))

  val T = Text(s1 :: ♯ :: ♯ :: s2 :: ♯ :: s2 :: s2 :: Nil)

  test("Expression order and show") {
    import cats.Order.catsKernelOrderingForOrder
    T.expressions.sorted.show.length should be(157)
  }

  test("Show[Text]") {
    T.show.length should be(153)
  }

  test("Show[Morpheme]") {
    mHi.show should be("hi")
  }

  test("Show[Language]") {
    ℒ.show.length should be(129)
  }

  test("Vocabulary.size (via iterator)") {
    Σ.size should be(6)
  }

  test("Text.isFor(Language)") {
    T.isFor(ℒ) should be(true)
  }

  test("memorizing learner memorizes") {

    val ɸ = GoldParadigm.memorizingLearner

    val outcome = lastOption(ɸ.guesses(T))

    outcome.get.ℒ should be(ℒ)
  }

  test("hard-coded learner hard-codes") {

    val ɸ = GoldParadigm.hardCodedLearner(HardCodedGrammar(ℒ))

    val outcome = lastOption(ɸ.guesses(T))

    outcome.get.ℒ should be(ℒ)
  }

}
