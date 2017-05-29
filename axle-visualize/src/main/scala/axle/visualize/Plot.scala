package axle.visualize

import axle.algebra.LengthSpace
import axle.algebra.Tics
import axle.algebra.Zero
import axle.visualize.element.Text

import cats.Show
import cats.kernel.Eq

case class Plot[S, X, Y, D](
    dataFn: Function0[Seq[(S, D)]],
    connect: Boolean = true,
    drawKey: Boolean = true,
    width: Int = 700,
    height: Int = 600,
    border: Int = 50,
    pointDiameter: Int = 4,
    keyLeftPadding: Int = 20,
    keyTopPadding: Int = 50,
    keyWidth: Int = 80,
    fontName: String = "Courier New",
    fontSize: Int = 12,
    bold: Boolean = false,
    titleFontName: String = "Palatino",
    titleFontSize: Int = 20,
    colorOf: S => Color,
    title: Option[String] = None,
    keyTitle: Option[String] = None,
    xAxis: Option[Y] = None,
    xAxisLabel: Option[String] = None,
    yAxis: Option[X] = None,
    yAxisLabel: Option[String] = None)(
        implicit val eqS: Eq[S],
        val eqD: Eq[D],
        val sShow: Show[S],
        val xZero: Zero[X],
        val xts: Tics[X],
        val xEq: Eq[X],
        val xLength: LengthSpace[X, _, Double],
        val yZero: Zero[Y],
        val yts: Tics[Y],
        val yEq: Eq[Y],
        val yLength: LengthSpace[Y, _, Double],
        val plotDataView: PlotDataView[S, X, Y, D]) {

  val xAxisLabelText = xAxisLabel.map(Text(_, width / 2, height - border / 2, fontName, fontSize, bold=true))

  val yAxisLabelText = yAxisLabel.map(Text(_,  20, height / 2, fontName, fontSize, bold=true, angle = Some(90d *: angleDouble.degree)))

  val titleText = title.map(Text(_, width / 2, titleFontSize, titleFontName, titleFontSize, bold=true))

}
