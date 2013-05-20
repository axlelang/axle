package axle.ml

abstract class Classifier[D, TF, TC](
  classExtractor: D => TC) {

  def predict(d: D): TC

  /**
   * For a given class (label value), predictedVsActual returns a tally of 4 cases:
   *
   * 1. true positive
   * 2. false positive
   * 3. false negative
   * 4. true negative
   *
   */

  import axle.algebra._
  import Semigroups._

  private[this] def predictedVsActual(dit: Iterator[D], k: TC): (Int, Int, Int, Int) = dit.map(d => {
    val actual = classExtractor(d)
    val predicted = predict(d)
    (actual === k, predicted === k) match {
      case (true, true) => (1, 0, 0, 0) // true positive
      case (false, true) => (0, 1, 0, 0) // false positive
      case (false, false) => (0, 0, 1, 0) // false negative
      case (true, false) => (0, 0, 0, 1) // true negative
    }
  }).reduce(_ |+| _)

  def performance(dit: Iterator[D], k: TC) = {

    val (tp, fp, fn, tn) = predictedVsActual(dit, k)

    ClassifierPerformance(
      tp.toDouble / (tp + fp), // precision
      tp.toDouble / (tp + fn), // recall
      tn.toDouble / (tn + fp), // specificity aka "true negative rate"
      (tp + tn).toDouble / (tp + tn + fp + fn) // accuracy
    )
  }

}