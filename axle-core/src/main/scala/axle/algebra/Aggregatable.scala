package axle.algebra

import scala.annotation.implicitNotFound

@implicitNotFound("Witness not found for Aggregatable[${F}]")
trait Aggregatable[F[_]] {

  def aggregate[A, B](xs: F[A])(zeroValue: B)(seqOp: (B, A) => B, combOp: (B, B) => B): B

}

object Aggregatable {

  def apply[F[_]](implicit aggFA: Aggregatable[F]): Aggregatable[F] =
    implicitly[Aggregatable[F]]

  implicit val aggregatableSeq =
    new Aggregatable[Seq] {
      def aggregate[A, B](as: Seq[A])(zeroValue: B)(seqOp: (B, A) => B, combOp: (B, B) => B): B =
        as.foldLeft(zeroValue)(seqOp)
    }

  implicit val aggregatableList =
    new Aggregatable[List] {
      def aggregate[A, B](as: List[A])(zeroValue: B)(seqOp: (B, A) => B, combOp: (B, B) => B): B =
        as.foldLeft(zeroValue)(seqOp)
    }

  implicit val aggregatableVector =
    new Aggregatable[Vector] {
      def aggregate[A, B](as: Vector[A])(zeroValue: B)(seqOp: (B, A) => B, combOp: (B, B) => B): B =
        as.foldLeft(zeroValue)(seqOp)
    }

  implicit val aggregatableIndexedSeq =
    new Aggregatable[IndexedSeq] {
      def aggregate[A, B](is: IndexedSeq[A])(zeroValue: B)(seqOp: (B, A) => B, combOp: (B, B) => B): B =
        is.foldLeft(zeroValue)(seqOp)
    }

  implicit val aggregatableIterable =
    new Aggregatable[Iterable] {
      def aggregate[A, B](i: Iterable[A])(zeroValue: B)(seqOp: (B, A) => B, combOp: (B, B) => B): B =
        i.foldLeft(zeroValue)(seqOp)
    }

  implicit val aggregatableSet =
    new Aggregatable[Set] {
      def aggregate[A, B](s: Set[A])(zeroValue: B)(seqOp: (B, A) => B, combOp: (B, B) => B): B =
        s.foldLeft(zeroValue)(seqOp)
    }

}
