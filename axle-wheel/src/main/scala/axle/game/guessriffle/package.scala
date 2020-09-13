
package axle.game

import scala.util.Try

import cats.implicits._

import spire.math.Rational
import spire.random.Dist
import spire.random.Generator.rng

import axle.probability._
// import axle.probability.ConditionalProbabilityTable
// import axle.probability.rationalProbabilityDist

import axle.game.cards._

package object guessriffle {

    implicit val evGame: Game[GuessRiffle, GuessRiffleState, GuessRiffleOutcome, GuessRiffleMove, GuessRiffleState, Option[GuessRiffleMove], Rational, ConditionalProbabilityTable] =
    new Game[GuessRiffle, GuessRiffleState, GuessRiffleOutcome, GuessRiffleMove, GuessRiffleState, Option[GuessRiffleMove], Rational, ConditionalProbabilityTable] {

      def probabilityDist: Dist[Rational] = rationalProbabilityDist

      def startState(game: GuessRiffle): GuessRiffleState =
        GuessRiffleState(Deck(), None, None, List.empty, List.empty, 0, None)

      def startFrom(game: GuessRiffle, s: GuessRiffleState): Option[GuessRiffleState] =
        Some(startState(game))

      def players(game: GuessRiffle): IndexedSeq[Player] =
        Vector(GuessRiffle.dealer, game.player)

      def strategyFor(
        game:   GuessRiffle,
        player: Player): (GuessRiffle, GuessRiffleState) => ConditionalProbabilityTable[GuessRiffleMove, Rational] =
        player match {
          case game.player => game.strategy
          case GuessRiffle.dealer => GuessRiffle.dealerStrategy
          case _           => game.strategy // TODO unreachable
        }

      def isValid(
        g:     GuessRiffle,
        state: GuessRiffleState,
        move:  GuessRiffleMove): Either[String, GuessRiffleMove] =
        Right(move) // TODO

      def applyMove(
        game:  GuessRiffle,
        state: GuessRiffleState,
        move:  GuessRiffleMove): GuessRiffleState =
        move match {
          case Riffle() => {
            val riffled = Deck.riffleShuffle(state.initialDeck, rng)
            state.copy(riffledDeck = Some(riffled), remaining = riffled.cards)
          }
          case GuessCard(card) => state.copy(guess = Some(card))
          case RevealAndScore() => {
            val guessedCard = state.guess.get // "non-empty" assumption
            val revealedCard = state.remaining.head // "non-empty" assumption
            val newHistory = revealedCard :: state.history
            if( revealedCard === guessedCard ) {
              state.copy(guess = None, history = newHistory, remaining = state.remaining.tail, numCorrect = state.numCorrect + 1)
            } else {
              state.copy(guess = None, history = newHistory, remaining = state.remaining.tail)
            }
          }
        }

      def mover(
        game: GuessRiffle,
        s:    GuessRiffleState): Option[Player] =
        if (s.riffledDeck.isEmpty) {
          Some(GuessRiffle.dealer)
        } else if ( s.guess.isEmpty ) {
          if( s.remaining.size == 0) {
            None
          } else {
            Some(game.player)
          }
        } else {
          Some(GuessRiffle.dealer)
        }

      def moverM(
        game: GuessRiffle,
        s:    GuessRiffleState): Option[Player] =
        mover(game, s)

      def moves(
        game: GuessRiffle,
        s:    GuessRiffleState): Seq[GuessRiffleMove] =
        if ( s.riffledDeck.isEmpty ) {
          List(Riffle())
        } else if ( s.guess.isEmpty ) {
          (s.initialDeck.cards.toSet -- s.history).toList.map(GuessCard)
        } else {
          List(RevealAndScore())
        }
 
      def maskState(game: GuessRiffle, state: GuessRiffleState, observer: Player): GuessRiffleState =
        if (observer === game.player) {
          state.copy(remaining = List.empty)
        } else {
          state
        }

      def maskMove(game: GuessRiffle, move: GuessRiffleMove, mover: Player, observer: Player): Option[GuessRiffleMove] =
        Some(move)

      def outcome(
        game:  GuessRiffle,
        state: GuessRiffleState): Option[GuessRiffleOutcome] =
        if( state.riffledDeck.isEmpty || state.remaining.size > 0) {
          None
        } else {
          Some(GuessRiffleOutcome(state.numCorrect))
        }
 
      implicit def sampler = ConditionalProbabilityTable.samplerWitness

    }

    implicit val evGameIO: GameIO[GuessRiffle, GuessRiffleOutcome, GuessRiffleMove, GuessRiffleState, Option[GuessRiffleMove]] =
    new GameIO[GuessRiffle, GuessRiffleOutcome, GuessRiffleMove, GuessRiffleState, Option[GuessRiffleMove]] {

      def displayerFor(g: GuessRiffle, player: Player): String => Unit =
        player match {
          case g.player => g.displayer
          case GuessRiffle.dealer => g.dealerDisplayer
          case _        => g.displayer
        }

      def parseMove(g: GuessRiffle, input: String): Either[String, GuessRiffleMove] =
        if(input == "riffle") {
          Right(Riffle())
        } else if (input == "reveal" ) {
          Right(RevealAndScore())
        } else {
          Try(GuessCard(Card(Rank(input(0)), Suit(input(1))))).toEither.left.map(throwable => input + " is not a valid move.  Please select again")
        }

      def introMessage(ttt: GuessRiffle) =
        "Guess Riffle Shuffle"

      def displayStateTo(game: GuessRiffle, s: GuessRiffleState, observer: Player): String = {
        if (observer === game.player) {
          s"${s.numCorrect} correct ${s.initialDeck.cards.size - s.remaining.size - s.numCorrect} incorrect with ${s.remaining.size} cards remaining"
        } else {
          s"${s.numCorrect} correct ${s.initialDeck.cards.size - s.remaining.size - s.numCorrect} incorrect with ${s.remaining.size} cards remaining"
        }
      }

      def displayMoveTo(
        game:     GuessRiffle,
        move:     Option[GuessRiffleMove],
        mover:    Player,
        observer: Player): String =
        move.map { m => m match {
            case GuessCard(card) => mover.referenceFor(observer) + " guessed " + card.show
            case Riffle() => "riffle"
            case RevealAndScore() => "revealed top card"
          }
        } getOrElse("undefined")

      def displayOutcomeTo(
        game:     GuessRiffle,
        outcome:  GuessRiffleOutcome,
        observer: Player): String =
        game.player.referenceFor(observer) + s"${outcome.numCorrect} correct"

    }
}