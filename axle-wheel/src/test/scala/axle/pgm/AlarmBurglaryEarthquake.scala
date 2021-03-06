package axle.pgm

import edu.uci.ics.jung.graph.DirectedSparseGraph
import cats.implicits._
import cats.effect.IO
import spire.math._

import axle.probability._
import axle.laws._
import axle.laws.generator._
import axle.algebra.Region
import axle.example.AlarmBurglaryEarthquakeBayesianNetwork
import axle.pgm.MonotypeBayesanNetwork

import org.scalacheck.Gen
import org.scalacheck.Arbitrary
import org.scalatest.funsuite._
import org.scalatest.matchers.should.Matchers

class AlarmBurglaryEarthQuakeBayesianNetworkIsKolmogorov
  extends KolmogorovProbabilityProperties[
    Rational,
    ({ type L[C, W] = MonotypeBayesanNetwork[C, Boolean, W, DirectedSparseGraph] })#L,
    (Boolean, Boolean, Boolean, Boolean, Boolean),
    Rational](
    "Alarm-Burglary-Earthquake Bayesian Network",
    // Arbitrary[T] -- arbitrary seed
    // TODO non-1 numerators
    Arbitrary(for {
      denominator <- Gen.oneOf(1 to 1000)
      numerator <- Gen.oneOf(1 to denominator)
    } yield Rational(numerator.toLong, denominator.toLong)),
    // T => M[E, V]
    { case seed => new AlarmBurglaryEarthquakeBayesianNetwork(pEarthquake = seed).monotype },
    { case seed => Arbitrary(genRegion(AlarmBurglaryEarthquakeBayesianNetwork.domain)) },
    { case seed => Region.eqRegionIterable(AlarmBurglaryEarthquakeBayesianNetwork.domain) }
  )(
    axle.pgm.MonotypeBayesanNetwork.kolmogorovWitness[Boolean, DirectedSparseGraph],
    cats.kernel.Eq[(Boolean, Boolean, Boolean, Boolean, Boolean)],
    spire.algebra.Field[Rational],
    cats.kernel.Order[Rational]
  )

class AlarmBurglaryEarthquakeBayesianNetworkIsBayes
  extends BayesTheoremProperty[
    Rational,
    ({ type L[C, W] = MonotypeBayesanNetwork[C, Boolean, W, DirectedSparseGraph] })#L,
    (Boolean, Boolean, Boolean, Boolean, Boolean),
    Rational](
    "Alarm-Burglary-Earthquake Bayesian Network",
    Arbitrary(genPortion),
    { case seed => new AlarmBurglaryEarthquakeBayesianNetwork(pEarthquake = seed).monotype },
    { case seed => Arbitrary(genRegion(AlarmBurglaryEarthquakeBayesianNetwork.domain)) },
    { case seed => Region.eqRegionIterable(AlarmBurglaryEarthquakeBayesianNetwork.domain) }
)(
    axle.pgm.MonotypeBayesanNetwork.bayesWitness[Boolean, DirectedSparseGraph],
    axle.pgm.MonotypeBayesanNetwork.kolmogorovWitness[Boolean, DirectedSparseGraph],
    cats.kernel.Eq[(Boolean, Boolean, Boolean, Boolean, Boolean)],
    spire.algebra.Field[Rational],
    cats.kernel.Order[Rational]
)

class AlarmBurglaryEarthquakeSpec extends AnyFunSuite with Matchers {

  implicit val showRat = cats.Show.fromToString[Rational]

  val abe = new AlarmBurglaryEarthquakeBayesianNetwork()

  import AlarmBurglaryEarthquakeBayesianNetwork._
  import abe._

  test("bayesian networks produces a Joint Probability Table, which is '1' when all variables are removed") {

    val jpt = bn.jointProbabilityTable

    val sansAll: Factor[Boolean, Rational] = jpt.Σ(M).Σ(J).Σ(A).Σ(B).Σ(E)

    import spire.implicits.multiplicativeSemigroupOps
    (bn.factorFor(A) * bn.factorFor(B)) * bn.factorFor(E) // dropping "abe"

    // val Q: Set[Variable[Boolean]] = Set(E, B, A)
    // val order = List(J, M)

    // val afterVE = bn.variableEliminationPriorMarginalI(Q, order)
    // val afterVE = bn.variableEliminationPriorMarginalII(Q, order, E is true)
    // bn.getDistributions.map(rv => println(bn.getMarkovAssumptionsFor(rv)))
    // println("P(B) = " + ans1) // 0.001
    // println("P(A| B, -E) = " + ans2) // 0.94
    // println("eliminating variables other than A, B, and E; and then finding those consistent with E = true")
    // println(afterVE)

    sansAll.apply(Vector.empty) should be(Rational(1))
    sansAll.evaluate(Seq.empty, Seq.empty) should be(Rational(1))
  }

  test("bayesian network visualization") {

    import axle.visualize._

    //val pngGName = "bnGraph.png"
    val svgGName = "bnGraph.svg"
    val graphVis = DirectedGraphVisualization[DirectedSparseGraph[BayesianNetworkNode[Boolean, Rational], Edge], BayesianNetworkNode[Boolean, Rational], Edge](
      bn.graph, 200, 200, 10)

    //val pngName = "bn.png"
    val svgName = "bn.svg"
    val vis = BayesianNetworkVisualization[Boolean, Rational, DirectedSparseGraph[BayesianNetworkNode[Boolean, Rational], Edge]](bn, 200, 200, 10)

    //import axle.awt._
    import axle.web._

    (for {
      //_ <- graphVis.png[IO](pngGName)
      _ <- graphVis.svg[IO](svgGName)
      //_ <- vis.png[IO](pngName)
      _ <- vis.svg[IO](svgName)
    } yield ()).unsafeRunSync()

    //new java.io.File(pngGName).exists should be(true)
    new java.io.File(svgGName).exists should be(true)
    //new java.io.File(pngName).exists should be(true)
    new java.io.File(svgName).exists should be(true)

  }
}
