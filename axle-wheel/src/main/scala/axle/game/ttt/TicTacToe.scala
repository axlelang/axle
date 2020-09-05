
package axle.game.ttt

import cats.implicits._

import spire.math.Rational

import axle.game._
import axle.probability.ConditionalProbabilityTable

/**
 * TicTacToe is a 2-player perfect information zero-sum game
 */

case class TicTacToe(
  boardSize:  Int                                                                                  = 3,
  x:          Player,
  xStrategy:  (TicTacToe, TicTacToeState) => ConditionalProbabilityTable[TicTacToeMove, Rational],
  xDisplayer: String => Unit,
  o:          Player,
  oStrategy:  (TicTacToe, TicTacToeState) => ConditionalProbabilityTable[TicTacToeMove, Rational],
  oDisplayer: String => Unit) {

  val players = Vector(x, o)

  val playerToDisplayer = Map(x -> xDisplayer, o -> oDisplayer)

  val playerToStrategy = Map(x -> xStrategy, o -> oStrategy)

  def numPositions: Int = boardSize * boardSize

  def startBoard: Array[Option[Player]] =
    (0 until (boardSize * boardSize)).map(i => None).toArray

  def playerAfter(player: Player): Player =
    if (player === x) o else x

  def markFor(player: Player): Char =
    if (player === x) 'X' else 'O'

}
