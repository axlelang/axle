package axle.quanta

import axle.algebra.Bijection
import axle.algebra.DirectedGraph
import spire.algebra.Eq
import spire.algebra.Field

case class MoneyFlow() extends Quantum {

  def wikipediaUrl: String = ""

}

trait MoneyFlowUnits extends QuantumUnits[MoneyFlow] {

  lazy val USDperHour = unit("$/hr", "$/hr") // derive

  def units: List[UnitOfMeasurement[MoneyFlow]] =
    List(USDperHour)

}

trait MoneyFlowConverter[N] extends UnitConverter[MoneyFlow, N] with MoneyFlowUnits {

  def defaultUnit = USDperHour
}

object MoneyFlow {

  def converterGraph[N: Field: Eq, DG](implicit evDG: DirectedGraph[DG, UnitOfMeasurement[MoneyFlow], N => N]) =
    new UnitConverterGraph[MoneyFlow, N, DG] with MoneyFlowConverter[N] {

      def links: Seq[(UnitOfMeasurement[MoneyFlow], UnitOfMeasurement[MoneyFlow], Bijection[N, N])] =
        List.empty

    }

}