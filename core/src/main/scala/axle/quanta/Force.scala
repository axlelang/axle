package axle.quanta

import java.math.BigDecimal
import axle.graph.JungDirectedGraph._

class Force extends Quantum {

  class ForceQuantity(
    magnitude: BigDecimal = oneBD,
    _unit: Option[Q] = None,
    _name: Option[String] = None,
    _symbol: Option[String] = None,
    _link: Option[String] = None) extends Quantity(magnitude, _unit, _name, _symbol, _link)

  type Q = ForceQuantity

  def newUnitOfMeasurement(
    name: Option[String] = None,
    symbol: Option[String] = None,
    link: Option[String] = None): ForceQuantity =
    new ForceQuantity(oneBD, None, name, symbol, link)

  def newQuantity(magnitude: BigDecimal, unit: ForceQuantity): ForceQuantity =
    new ForceQuantity(magnitude, Some(unit), None, None, None)

  def conversionGraph() = _conversionGraph

  val wikipediaUrl = "http://en.wikipedia.org/wiki/Force"

  lazy val _conversionGraph = JungDirectedGraph[ForceQuantity, BigDecimal](
    List(
      unit("pound", "lb", Some("http://en.wikipedia.org/wiki/Pound-force")),
      unit("newton", "N", Some("http://en.wikipedia.org/wiki/Newton_(unit)")),
      unit("dyne", "dyn", Some("http://en.wikipedia.org/wiki/Dyne"))
    ),
    (vs: Seq[JungDirectedGraphVertex[ForceQuantity]]) => vs match {
      case Nil => List()
    }
  )

  lazy val pound = byName("pound")
  lazy val newton = byName("newton")
  lazy val dyne = byName("dyne")

}

object Force extends Force()
