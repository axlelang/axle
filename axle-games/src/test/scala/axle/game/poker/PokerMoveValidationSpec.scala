package axle.game.poker

import org.specs2.mutable._
import axle.game._

class PokerMoveValidationSpec extends Specification {

  val p1 = Player("P1", "Player 1")
  val p2 = Player("P2", "Player 2")

  val game = Poker(Vector(
    (p1, randomMove, println),
    (p2, randomMove, println)),
    println)

  "move validator" should {
    "reject River as first move" in {

      Poker.evGame.isValid(game, startState(game), River()) must be equalTo Left("invalid move")
    }
  }
}