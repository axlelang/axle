package axle.game.ttt

import org.scalatest.funsuite._
import org.scalatest.matchers.should.Matchers

import cats.implicits._

import spire.random.Generator.rng
import spire.math.Rational

import axle.syntax.sampler._
import axle.probability._
import axle.game._
import axle.game.Strategies._

class TicTacToeSpec extends AnyFunSuite with Matchers {

  import axle.game.ttt.evGame._
  import axle.game.ttt.evGameIO._

  implicit val rat = new spire.math.RationalAlgebra()
  val monadCptRat = ConditionalProbabilityTable.monadWitness[Rational]

  val x = Player("X", "Player X")
  val o = Player("O", "Player O")

  val game = TicTacToe(3, x, o)

  val randomMove =
    (state: TicTacToeState) =>
      ConditionalProbabilityTable.uniform[TicTacToeMove, Rational](evGame.moves(game, state))

  test("game define intro message, have 9 positions") {

    introMessage(game) should include("Moves are")
    game.numPositions should be(9)
  }

  test("random game produce moveStateStream") {

   val fMSS = moveStateStream[
      TicTacToe,
      TicTacToeState,
      TicTacToeOutcome,
      TicTacToeMove,
      TicTacToeState,
      TicTacToeMove,
      Rational,
      ConditionalProbabilityTable,
      Option](game, startState(game), _ => randomMove.andThen(Option.apply), rng)
      
     fMSS.get.take(3).length should be(3)
  }

  test("random game plays") {

    val fEndState = play(
      game,
      _ => randomMove.andThen(Option.apply),
      startState(game),
      rng).get

    moves(game, fEndState).length should be(0)
  }

  test("random game produce game stream") {

    val games = gameStream(
      game,
      _ => randomMove.andThen(Option.apply),
      startState(game),
      i => i < 10,
      rng).get

    games should have length 10
  }

  test("start state display movement key to player x, and have 9 moves available to x") {

    displayStateTo(game, startState(game), x) should include("Movement Key")
  }

  test("startFrom return the start state") {

    val state = startState(game)
    val move = moves(game, state).head
    val nextState = applyMove(game, state, move)
    val newStart = startFrom(game, nextState).get

    moves(game, newStart).length should be(9)
    mover(game, state).isRight should be(true)
  }

  test("starting moves are nine-fold, display to O with 'put an', and have string descriptions that include 'upper'") {

    val startingMoves = moves(game, startState(game))

    displayMoveTo(game, startingMoves.head, x, o) should include("put an")
    startingMoves.length should be(9)
    startingMoves.map(_.description).mkString(",") should include("upper")
  }

  test("starting moves are defined for 4x4 game") {
    val bigGame = TicTacToe(4, x, o)
    val startingMoves = moves(bigGame, startState(bigGame))
    startingMoves.map(_.description).mkString(",") should include("16")
  }

  test("interactive player produces messages") {

    val firstMove = TicTacToeMove(2, game.boardSize)
    val secondState = applyMove(game, startState(game), firstMove)

    // val m = secondState.moverOpt.get
    evGameIO.parseMove(game, "14") should be(Left("Please enter a number between 1 and 9"))
    evGameIO.parseMove(game, "foo") should be(Left("foo is not a valid move.  Please select again"))

    evGameIO.parseMove(game, "1").flatMap(move => evGame.isValid(game, secondState, move)).toOption.get.position should be(1)
    evGameIO.parseMove(game, "2").flatMap(move => evGame.isValid(game, secondState, move)) should be(Left("That space is occupied."))
  }

  test("random strategy makes a move") {

    implicit val rat = new spire.math.RationalAlgebra()

    val mover = randomMove.andThen(Option.apply)
    val moveCpt = mover(startState(game))
    val m = moveCpt.get.sample(rng)

    m.position should be > 0
  }

  test("A.I. strategy makes a move") {

    import cats.implicits._ // for Order[Double]

    import spire.algebra.Field

    val firstMove = TicTacToeMove(2, game.boardSize)

    val h = (outcome: TicTacToeOutcome, p: Player) =>
      outcome.winner.map(wp => if (wp == p) 1d else -1d).getOrElse(0d)

    implicit val fieldDouble: Field[Double] = spire.implicits.DoubleAlgebra
    val ai4 = aiMover[TicTacToe, TicTacToeState, TicTacToeOutcome, TicTacToeMove, TicTacToeState, TicTacToeMove, Double](
      game,
      ms => ms,
      4,
      outcomeRingHeuristic(game, h))

    val secondState = applyMove(game, startState(game), firstMove)
    val move = ai4(secondState)

    move.position should be > 0
  }

  test("7-move x diagonal be a victory for x") {

    def xMove(game: TicTacToe, state: TicTacToeState): String =
      moves(game, state).size match {
        case 9 => "1"
        case 7 => "3"
        case 5 => "5"
        case 3 => "7"
      }

    def oMove(game: TicTacToe, state: TicTacToeState): String =
      moves(game, state).size match {
        case 8 => "2"
        case 6 => "4"
        case 4 => "6"
      }

    def strategyFor(player: Player): TicTacToeState => ConditionalProbabilityTable[TicTacToeMove, Rational] =
      (if ( player == x ) {
        hardCodedStringStrategy(game)(xMove)
      } else if ( player == o ) {
        hardCodedStringStrategy(game)(oMove)
      } else {
        ???
      }).andThen(monadCptRat.pure)

    val start = startState(game)

    val mss = moveStateStream(
      game,
      start,
      player => strategyFor(player).andThen(Option.apply _),
      rng).get

    val out = mover(game, mss.last._3).swap.toOption.get

    displayOutcomeTo(game, out, x) should include("You beat")
    displayOutcomeTo(game, out, o) should include("beat You")
    out.winner.get should be(x)
  }

  test("7-move o diagonal is a victory for o") {

    def xMove(game: TicTacToe, state: TicTacToeState): String =
      moves(game, state).size match {
        case 9 => "2"
        case 7 => "4"
        case 5 => "6"
        case 3 => "8"
      }

    def oMove(game: TicTacToe, state: TicTacToeState): String =
      moves(game, state).size match {
        case 8 => "3"
        case 6 => "5"
        case 4 => "7"
      }

    def strategyFor(player: Player): TicTacToeState => ConditionalProbabilityTable[TicTacToeMove, Rational] =
      (if ( player == x ) {
        hardCodedStringStrategy(game)(xMove)
      } else if ( player == o ) {
        hardCodedStringStrategy(game)(oMove)
      } else {
        ???
      }).andThen(monadCptRat.pure)

    val start = startState(game)

    val lastState = moveStateStream(
      game,
      start,
      p => strategyFor(p).andThen(Option.apply _),
      rng).get.last._3

    val winnerOpt = mover(game, lastState).swap.toOption.flatMap(_.winner)
    winnerOpt should be(Some(o))
  }

  test("9 move tie result in no-winner outcome") {

    def xMove(game: TicTacToe, state: TicTacToeState): String =
      moves(game, state).size match {
        case 9 => "1"
        case 7 => "3"
        case 5 => "5"
        case 3 => "8"
        case 1 => "6"
      }

    def oMove(game: TicTacToe, state: TicTacToeState): String =
      moves(game, state).size match {
        case 8 => "2"
        case 6 => "4"
        case 4 => "7"
        case 2 => "9"
      }

    def strategyFor(player: Player): TicTacToeState => ConditionalProbabilityTable[TicTacToeMove, Rational] =
      (if ( player == x ) {
        hardCodedStringStrategy(game)(xMove)
      } else if ( player == o ) {
        hardCodedStringStrategy(game)(oMove)
      } else {
        ???
      }).andThen(monadCptRat.pure)

    val start = startState(game)

    val mss = moveStateStream(
      game,
      start,
      p => strategyFor(p).andThen(Option.apply _),
      rng).get

    val winnerOpt = mover(game, mss.last._3).swap.toOption.flatMap(_.winner)
    winnerOpt should be(None)
  }

}
