package axle

import scala.reflect.ClassTag
import cats.implicits._

case class EnrichedArray[T: ClassTag](arr: Array[T]) {

  def apply(range: Range): Array[T] = {
    assert(range.step === 1)
    if (range.isEmpty) {
      List[T]().toArray
    } else {
      arr.slice(range.start, range.last + 1)
    }
  }

  def swap(i0: Int, i1: Int): Array[T] = {
    val result = arr.clone
    result(i0) = arr(i1)
    result(i1) = arr(i0)
    result
  }

}
