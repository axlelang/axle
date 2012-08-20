package axle.stats

import axle.iterator.ListCrossProduct
import axle.matrix.JblasMatrixFactory._
import collection._

object Factor {

  def multiply(tables: Seq[Factor]): Factor = {

    if (tables.size == 0) {
      return null
    }

    // TODO this can be made more efficient by constructing a single
    // result table ahead of time.

    tables.reduceLeft((current, table) => current.multiply(table))
  }

}

/* Technically a "Distribution" is probably a table that sums to 1, which is not
 * always true in a Factor.  They should be siblings rather than parent/child.
 */

class Factor(varList: List[RandomVariable[_]], name: String = "unnamed") extends DistributionX(varList) {

  import scalaz._
  import Scalaz._

  val valLists = varList.map(rv => rv.getValues.getOrElse(Nil).toList)
  val cp = new ListCrossProduct(valLists)
  val elements = new Array[Double](cp.size)

  // var name = "unnamed"
  // def setName(name: String): Unit = { this.name = name }

  def getName(): String = name

  def getLabel(): String = name

  // assume prior and condition are disjoint, and that they are
  // each compatible with this table
  def evaluate(prior: CaseX, condition: CaseX): Double = {
    val pw = (0 until numCases).map(i => {
      val c = caseOf(i)
      if (c.isSupersetOf(prior)) {
        if (c.isSupersetOf(condition)) {
          (this(c), this(c))
        } else {
          (this(c), 0.0)
        }
      } else {
        (0.0, 0.0)
      }
    }).reduce(_ |+| _)

    pw._1 / pw._2
  }

  def indexOf(c: CaseX): Int = cp.indexOf(c.valuesOf(varList))

  def caseOf(i: Int): CaseX = {
    val result = new CaseX()
    val values = cp(i)
    result.assign(varList, values)
    result
  }

  def numCases() = elements.length

  def update(c: CaseX, d: Double): Unit = {
    // println("write: case = " + c.toOrderedString(variables) + ", d = " + d)
    // println("variables.length = " + variables.length)
    elements(indexOf(c)) = d
  }

  def writes(values: List[Double]): Unit = {
    assert(values.length == elements.length)
    values.zipWithIndex.map({ case (v, i) => elements(i) = v })
  }

  def apply(c: CaseX): Double = elements(indexOf(c))

  def print(): Unit = {
    for (i <- 0 until elements.length) {
      val c = caseOf(i)
      println(c.toOrderedString(varList) + " " + this(c))
    }
  }

  // Chapter 6 definition 6
  def maxOut[T](variable: RandomVariable[T]): Factor = {
    val vars = getVariables.filter(!variable.equals(_))
    val newFactor = new Factor(vars)
    for (i <- 0 until newFactor.numCases()) {
      def ci = newFactor.caseOf(i)
      val maxSoFar = variable.getValues.getOrElse(Nil).map(value => this(newFactor.caseOf(i))).max
      newFactor(ci) = maxSoFar
    }
    newFactor
  }

  def projectToOnly(remainingVars: List[RandomVariable[_]]): Factor = {
    val result = new Factor(remainingVars)
    for (j <- 0 until numCases) {
      val fromCase = this.caseOf(j)
      val toCase = fromCase.projectToVars(remainingVars)
      val additional = this(fromCase)
      val previous = result(toCase)
      result(toCase) = previous + additional
    }
    result
  }

  def tally[A, B](a: RandomVariable[A], b: RandomVariable[B]): Matrix[Double] = {
    val aValues = a.getValues.getOrElse(Nil).toList
    val bValues = b.getValues.getOrElse(Nil).toList
    val tally = zeros[Double](aValues.size, bValues.size)
    val w = new CaseX()
    aValues.zipWithIndex.map({
      case (aVal, r) => {
        w.assign(a, aVal)
        bValues.zipWithIndex.map({
          case (bVal, c) => {
            w.assign(b, bVal)
            for (j <- 0 until numCases) {
              val m = caseOf(j)
              if (m.isSupersetOf(w)) {
                tally(r, c) += this(m)
              }
            }
          }
        })
      }
    })
    tally
  }

  // depending on assumptions, this may not be the best way to remove the vars
  def sumOut[T](varToSumOut: RandomVariable[T]): Factor = {
    val result = new Factor(getVariables().filter(!_.equals(varToSumOut)).toList)
    for (j <- 0 until result.numCases()) {
      val c = result.caseOf(j)
      val p = varToSumOut.getValues.getOrElse(Nil).map(value => {
        val f = c.copy
        f.assign(varToSumOut, value)
        this(f)
      }).sum
      result(c) = p
    }
    result
  }

  def sumOut(varsToSumOut: Set[RandomVariable[_]]): Factor =
    varsToSumOut.foldLeft(this)((result, v) => result.sumOut(v))

  // as defined on chapter 6 page 15
  def projectRowsConsistentWith(eOpt: Option[CaseX]): Factor = {
    val e = eOpt.get
    val result = new Factor(getVariables())
    for (j <- 0 until result.numCases) {
      val c = caseOf(j)
      result.elements(j) = (c.isSupersetOf(e) match {
        case true => elements(j)
        case false => 0.0
      })
    }
    result
  }

  def multiply(other: Factor): Factor = {

    val newVars = getVariables().union(other.getVariables())
    val result = new Factor(newVars.toList)
    for (j <- 0 until result.numCases()) {
      val c = result.caseOf(j)
      result(c) = this(c) * other(c)
    }
    result
  }

  def mentions(variable: RandomVariable[_]) = getVariables.exists(v => variable.getName.equals(v.getName))

}
