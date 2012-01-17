/*
 * Created on Jun 7, 2005
 *
 */
package org.pingel.causality.examples

import org.pingel.causality.CausalModel
import org.pingel.forms.Basic.PFunction
import org.pingel.bayes.ModelVisualizer
import org.pingel.bayes.RandomVariable

object Model3dot8e extends CausalModel("3.8e") {

	val X = addVariable(new RandomVariable("X"))
	val Y = addVariable(new RandomVariable("Y"))
    val Z = addVariable(new RandomVariable("Z"))
    val U = addVariable(new RandomVariable("U", None, false))

    addFunction(new PFunction(X, List(U)))
    addFunction(new PFunction(Y, List(Z, U)))
    addFunction(new PFunction(Z, List(X)))

  def main(args: Array[String]) {
    ModelVisualizer.draw(Model3dot8e)
  }

}
