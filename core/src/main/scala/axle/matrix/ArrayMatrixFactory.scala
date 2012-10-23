package axle.matrix

object ArrayMatrixFactory extends ArrayMatrixFactory {}

/**
 *
 * Discussion of ClassManifest here:
 *
 * http://www.scala-lang.org/docu/files/collections-api/collections_38.html
 *
 */

abstract class ArrayMatrixFactory extends MatrixFactory {

  factory =>

  type M[T] = ArrayMatrix[T]

  type E[T] = Unit

  class ArrayMatrixImpl[T: ClassManifest](_storage: Array[T], nRows: Int, nColumns: Int) extends ArrayMatrix[T] {

    def storage = _storage

    def rows = nRows
    def columns = nColumns
    def length = storage.length

    def apply(r: Int, c: Int): T = _storage(r * nColumns + c)
    // def updat(r: Int, c: Int, v: T) = _storage(r * nColumns + c) = v
    def toList(): List[T] = _storage.toList

    def column(c: Int) = matrix((0 until nRows).map(this(_, c)).toArray, nRows, 1)

    def row(r: Int) = matrix((0 until nColumns).map(this(r, _)).toArray, 1, nColumns)

    def isEmpty(): Boolean = false // TODO
    def isRowVector(): Boolean = columns == 1
    def isColumnVector(): Boolean = rows == 1
    def isVector(): Boolean = isRowVector || isColumnVector
    def isSquare(): Boolean = columns == rows
    def isScalar(): Boolean = isRowVector && isColumnVector

    def dup(): M[T] = matrix(_storage.clone, nRows, nColumns)
    def negate(): M[T] = null // TODO
    def transpose(): M[T] = null // TODO
    def diag(): M[T] = null // TODO
    def invert(): M[T] = null // TODO
    def ceil(): M[T] = null // TODO
    def floor(): M[T] = null // TODO
    def log(): M[T] = null // TODO
    def log10(): M[T] = null // TODO
    def fullSVD(): (M[T], M[T], M[T]) = null // TODO doesn't really make sense

    def pow(p: Double): M[T] = null // TODO

    def addScalar(x: T): M[T] = null // TODO
    def addAssignment(r: Int, c: Int, v: T): M[T] = {
      val length = rows * columns
      val arr = storage.clone
      arr(r * columns + c) = v
      matrix(arr, rows, columns)
    }
    def subtractScalar(x: T): M[T] = null // TODO
    def multiplyScalar(x: T): M[T] = null // TODO
    def divideScalar(x: T): M[T] = null // TODO
    def mulRow(i: Int, x: T): M[T] = null // TODO
    def mulColumn(i: Int, x: T): M[T] = null // TODO

    // Operations on pairs of matrices

    def addMatrix(other: M[T]): M[T] = null // TODO
    def subtractMatrix(other: M[T]): M[T] = null // TODO
    def multiplyMatrix(other: M[T]): M[T] = null // TODO
    def mulPointwise(other: M[T]) = null // TODO
    def divPointwise(other: M[T]) = null // TODO
    def concatenateHorizontally(right: M[T]): M[T] = null // TODO
    def concatenateVertically(under: M[T]): M[T] = null // TODO
    def solve(B: M[T]): M[T] = null // TODO // returns X, where this == A and A x X = B

    // Operations on a matrix and a column/row vector

    def addRowVector(row: M[T]): M[T] = null // TODO
    def addColumnVector(column: M[T]): M[T] = null // TODO
    def subRowVector(row: M[T]): M[T] = null // TODO
    def subColumnVector(column: M[T]): M[T] = null // TODO

    // Operations on pair of matrices that return M[Boolean]

    def lt(other: M[T]): Matrix[Boolean] = null // TODO
    def le(other: M[T]): Matrix[Boolean] = null // TODO
    def gt(other: M[T]): Matrix[Boolean] = null // TODO
    def ge(other: M[T]): Matrix[Boolean] = null // TODO
    def eq(other: M[T]): Matrix[Boolean] = null // TODO
    def ne(other: M[T]): Matrix[Boolean] = null // TODO

    def and(other: M[T]): Matrix[Boolean] = null // TODO
    def or(other: M[T]): Matrix[Boolean] = null // TODO
    def xor(other: M[T]): Matrix[Boolean] = null // TODO
    def not(): Matrix[Boolean] = null // TODO

    // various mins and maxs

    def max(): T = null.asInstanceOf[T] // TODO
    def argmax(): (Int, Int) = null // TODO
    def min(): T = null.asInstanceOf[T] // TODO
    def argmin(): (Int, Int) = null // TODO
    def columnMins(): M[T] = null // TODO
    def columnMaxs(): M[T] = null // TODO

    // In-place versions

    def ceili(): Unit = {} // TODO
    def floori(): Unit = {} // TODO
    def powi(p: Double): Unit = {} // TODO

    def addi(x: T): Unit = {} // TODO
    def subtracti(x: T): Unit = {} // TODO
    def multiplyi(x: T): Unit = {} // TODO
    def dividei(x: T): Unit = {} // TODO

    def addMatrixi(other: M[T]): Unit = {} // TODO
    def subtractMatrixi(other: M[T]): Unit = {} // TODO
    def addiRowVector(row: M[T]): Unit = {} // TODO
    def addiColumnVector(column: M[T]): Unit = {} // TODO
    def subiRowVector(row: M[T]): Unit = {} // TODO
    def subiColumnVector(column: M[T]): Unit = {} // TODO

    // higher order fuctions

    def map[B](f: T => B)(implicit elementAdapter: E[B]): M[B] = null // TODO
    // matrix(rows, columns, storage.map(f(_)))

    def flatMapColumns[A](f: M[T] => M[A])(implicit elementAdapter: E[A]): M[A] = null // TODO

  }

  trait ArrayMatrix[T] extends Matrix[T] {

    type S = Array[T]

    def toList(): List[T]
  }

  def matrix[T: ClassManifest](arr: Array[T], r: Int, c: Int): ArrayMatrix[T] = new ArrayMatrixImpl(arr, r, c)

  def matrix[T: ClassManifest](r: Int, c: Int, default: T): ArrayMatrix[T] = {
    val length = r * c
    val arr = new Array[T](length)
    0.until(length).map(i => arr(i) = default)
    matrix(arr, r, c)
  }

  def zeros[T](m: Int, n: Int)(implicit elementAdapter: E[T]): M[T] = null // TODO

  def matrix[T](m: Int, n: Int, values: Array[T])(implicit elementAdapter: E[T]): M[T] = null // TODO

  def matrix[T](m: Int, n: Int, topleft: => T, left: Int => T, top: Int => T, fill: (Int, Int, T, T, T) => T)(implicit elementAdapter: E[T]): M[T] = null // TODO

  def matrix[T](m: Int, n: Int, f: (Int, Int) => T)(implicit elementAdapter: E[T]): M[T] = null // TODO

}
