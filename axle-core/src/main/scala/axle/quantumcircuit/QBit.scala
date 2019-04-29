package axle.quantumcircuit

import cats.kernel.Eq

import spire.algebra._
import spire.math._

import axle.stats._


case class QBit[T: Field](a: Complex[T], b: Complex[T]) {

  require((a * a) + (b * b) === Complex(Field[T].one, Field[T].zero))

  def unindex: Vector[Complex[T]] = Vector(a, b)

  // A QBit (a b) collapses to
  // |0> with probability a^2
  // |1> with probability b^2

  def probabilityModel: ConditionalProbabilityTable[CBit, T] = {
    val m = Map[CBit, T](
      CBit0 -> (a * a).real,
      CBit1 -> (b * b).real
    )
    ConditionalProbabilityTable.apply(m, Variable("Q"))
  }
    
}


object QBit {

  implicit def eqQBit[T]: Eq[QBit[T]] =
    (x: QBit[T], y: QBit[T]) => (x.a === y.a && x.b === y.b)

  // Two operations on no bits

  def constant0[T](implicit fieldT: Field[T]): QBit[T] =
    QBit[T](Complex(fieldT.one), Complex(fieldT.zero))

  def constant1[T](implicit fieldT: Field[T]): QBit[T] =
    QBit[T](Complex(fieldT.zero), Complex(fieldT.one))

  // Four operations on 1 bit
  def identity[T](qbit: QBit[T]): QBit[T] = qbit

  def negate[T](qbit: QBit[T])(implicit fieldT: Field[T]): QBit[T] =
    QBit[T](qbit.b, qbit.a)

  def constant0[T](qbit: QBit[T])(implicit fieldT: Field[T]): QBit[T] =
    QBit[T](Complex(fieldT.one), Complex(fieldT.zero))

  def constant1[T](qbit: QBit[T])(implicit fieldT: Field[T]): QBit[T] =
    QBit[T](Complex(fieldT.zero), Complex(fieldT.one))

  def X[T](qbit: QBit[T])(implicit fieldT: Field[T]): QBit[T] =
    negate(qbit)


  /**
   * Hadamard
   *
   * Can be implemented with a 2x2 matrix:
   * 
   *   1/sqrt(2)  1/sqrt(2)
   *   1/sqrt(2) -1/sqrt(2)
   * 
   */

  def hadamard[T](qbit: QBit[T])(implicit fieldT: Field[T], nrootT: NRoot[T]): QBit[T] = {

    import spire.implicits._

    val two = fieldT.one + fieldT.one
    val sqrtHalf = Complex[T](fieldT.one / sqrt(two), fieldT.zero)

    QBit(
     sqrtHalf * qbit.a + sqrtHalf * qbit.b,
     sqrtHalf * qbit.a - sqrtHalf * qbit.b)
  }

  def H[T](qbit: QBit[T])(implicit fieldT: Field[T], nrootT: NRoot[T]): QBit[T] =
    hadamard(qbit)

}
