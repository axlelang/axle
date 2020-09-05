package axle.bio

import org.jblas.DoubleMatrix
import org.scalatest.funsuite._
import org.scalatest.matchers.should.Matchers
import org.scalacheck.Gen
import org.scalacheck.Arbitrary
import org.scalacheck.Properties
import org.scalacheck.Prop.forAll

import cats.implicits._

import spire.algebra._

import axle.algebra.functorIndexedSeq

object SharedNeedlemanWunsch {

  import NeedlemanWunschDefaults._

  implicit val ringInt: CRing[Int] = spire.implicits.IntAlgebra
  implicit val dim: CModule[Double, Int] = axle.algebra.modules.doubleIntModule

  implicit val laJblasDouble = {
    implicit val fieldDouble: Field[Double] = spire.implicits.DoubleAlgebra
    implicit val nrootDouble: NRoot[Double] = spire.implicits.DoubleAlgebra
    axle.jblas.linearAlgebraDoubleMatrix[Double]
  }

  implicit val space = NeedlemanWunschSimilaritySpace[IndexedSeq, Char, DoubleMatrix, Int, Double](
    similarity, gapPenalty)

}

class NeedlemanWunschSpec extends AnyFunSuite with Matchers {

  import NeedlemanWunsch.alignmentScore
  import NeedlemanWunsch.optimalAlignment
  import NeedlemanWunschDefaults._
  import SharedNeedlemanWunsch._

  test("Needleman-Wunsch DNA alignment") {

    val dna1 = "ATGCGGCC"
    val dna2 = "ATCGCCGG"
  
    val nwAlignment =
      optimalAlignment[IndexedSeq, Char, DoubleMatrix, Int, Double](
        dna1, dna2, similarity, gap, gapPenalty)
  
    val score = alignmentScore(
      nwAlignment._1,
      nwAlignment._2,
      gap,
      similarity,
      gapPenalty)
  
    nwAlignment should be(("ATGCGGCC--".toIndexedSeq, "AT-C-GCCGG".toIndexedSeq))
    score should be(32d)
    space.similarity(dna1, dna2) should be(score)
  }

}

class NeedlemanWunschLawfulSpec extends Properties("Needleman-Wunsch") {

  import SharedNeedlemanWunsch._

  implicit val genChar: Gen[Char] = Gen.oneOf('A', 'T', 'G', 'C')
  implicit val arbChar: Arbitrary[Char] = Arbitrary(genChar)

  property("most similar to itself") = forAll { (a: IndexedSeq[Char], b: IndexedSeq[Char]) =>
    (a == b) || (space.similarity(a, a) >= space.similarity(a, b))
  }

  property("symmetry") = forAll { (a: IndexedSeq[Char], b: IndexedSeq[Char]) =>
    space.similarity(a, b) == space.similarity(b, a)
  }
}