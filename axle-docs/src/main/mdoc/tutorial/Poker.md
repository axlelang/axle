---
layout: page
title: Poker
permalink: /tutorial/poker/
---

An N-Player, Imperfect Information, Zero-sum game

## Example

The `axle.game.cards` package models decks, cards, ranks, suits, and ordering.

Define a function that takes the hand size and returns the best 5-card hand

```scala mdoc
import cats.implicits._
import cats.Order.catsKernelOrderingForOrder

import axle.game.cards.Deck
import axle.game.poker.PokerHand

def winnerFromHandSize(handSize: Int) =
  Deck().cards.take(handSize).combinations(5).map(cs => PokerHand(cs.toVector)).toList.max

winnerFromHandSize(7).show
```

20 simulated 5-card hands made of 7-card hands.  Sorted.

```scala mdoc
val hands = (1 to 20).map(n => winnerFromHandSize(7)).sorted

hands.map({ hand => hand.show + "  " + hand.description }).mkString("\n")
```

Record 1000 simulated hands for each drawn hand size from 5 to 9

```scala mdoc
import axle.game.poker.PokerHandCategory

val data: IndexedSeq[(PokerHandCategory, Int)] =
  for {
    handSize <- 5 to 9
    trial <- 1 to 1000
  } yield (winnerFromHandSize(handSize).category, handSize)
```

BarChartGrouped to visualize the results

```scala mdoc
import spire.algebra.CRing

import axle.visualize.BarChartGrouped
import axle.visualize.Color._
import axle.syntax.talliable.talliableOps

implicit val ringInt: CRing[Int] = spire.implicits.IntAlgebra

val colors = List(black, red, blue, yellow, green)

val chart = BarChartGrouped[PokerHandCategory, Int, Int, Map[(PokerHandCategory, Int), Int], String](
  () => data.tally.withDefaultValue(0),
  title = Some("Poker Hands"),
  drawKey = false,
  yAxisLabel = Some("instances of category by hand size (1000 trials each)"),
  colorOf = (cat: PokerHandCategory, handSize: Int) => colors( (handSize - 5) % colors.size),
  hoverOf = (cat: PokerHandCategory, handSize: Int) => Some(s"${cat.show} from $handSize")
)
```

Render as SVG file

```scala mdoc
import axle.web._
import cats.effect._

chart.svg[IO]("poker_hands.svg").unsafeRunSync()
```

![poker hands](/tutorial/images/poker_hands.svg)

### Texas Hold 'Em Poker

As a game of "imperfect information", poker introduces the concept of Information Set.

```scala mdoc
import axle.game._
import axle.game.poker._

val p1 = Player("P1", "Player 1")
val p2 = Player("P2", "Player 2")

val game = Poker(Vector(p1, p2))
```

Create a `writer` for each player that prefixes the player id to all output.

```scala mdoc
import cats.effect.IO
import axle.IO.printMultiLinePrefixed

val playerToWriter: Map[Player, String => IO[Unit]] =
  evGame.players(game).map { player =>
    player -> (printMultiLinePrefixed[IO](player.id) _)
  } toMap
```

Use a uniform distribution on moves as the demo strategy:

```scala mdoc
import axle.probability._
import spire.math.Rational

val randomMove =
  (state: PokerStateMasked) =>
    ConditionalProbabilityTable.uniform[PokerMove, Rational](evGame.moves(game, state))
```

Wrap the strategies in the calls to `writer` that log the transitions from state to state.

```scala mdoc
val strategies: Player => PokerStateMasked => IO[ConditionalProbabilityTable[PokerMove, Rational]] = 
  (player: Player) =>
    (state: PokerStateMasked) =>
      for {
        _ <- playerToWriter(player)(evGameIO.displayStateTo(game, state, player))
        move <- randomMove.andThen( m => IO { m })(state)
      } yield move
```

Play the game -- compute the end state from the start state.

```scala mdoc
import spire.random.Generator.rng

val endState = play(game, strategies, evGame.startState(game), rng).unsafeRunSync()
```

Display outcome to each player

```scala mdoc
val outcome = evGame.mover(game, endState).swap.toOption.get

evGame.players(game).foreach { player =>
  playerToWriter(player)(evGameIO.displayOutcomeTo(game, outcome, player)).unsafeRunSync()
}
```
