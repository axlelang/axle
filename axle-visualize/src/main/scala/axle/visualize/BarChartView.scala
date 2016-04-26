package axle.visualize

import axle.algebra.Tics
import axle.string
import axle.visualize.Color.black
import axle.visualize.element.HorizontalLine
import axle.visualize.element.Rectangle
import axle.visualize.element.VerticalLine
import axle.visualize.element.XTics
import axle.visualize.element.YTics
import spire.compat.ordering
import spire.implicits.DoubleAlgebra

case class BarChartView[S, Y, D](
    chart: BarChart[S, Y, D],
    data: D) {

  import chart._

  val minX = 0d
  val maxX = 1d
  val yAxis = minX

  val slices = dataView.keys(data)

  val padding = 0.05 // on each side
  val widthPerSlice = (1d - (2 * padding)) / slices.size
  val whiteSpace = widthPerSlice * (1d - barWidthPercent)

  val (dataMinY, dataMaxY) = dataView.yRange(data)
  val minY = List(xAxis.getOrElse(zeroY.zero), dataMinY).min
  val maxY = List(xAxis.getOrElse(zeroY.zero), dataMaxY).max

  implicit val ddls = axle.algebra.LengthSpace.doubleDoubleLengthSpace

  val scaledArea = ScaledArea2D(
    width = if (drawKey) width - (keyWidth + keyLeftPadding) else width,
    height,
    border,
    minX, maxX, minY, maxY)

  val vLine = VerticalLine(scaledArea, yAxis, black)
  val hLine = HorizontalLine(scaledArea, xAxis.getOrElse(zeroY.zero), black)

  val gTics = XTics(
    scaledArea,
    slices.toStream.zipWithIndex.map({ case (s, i) => (padding + (i + 0.5) * widthPerSlice, string(s)) }).toList,
    normalFontName,
    normalFontSize,
    bold=true,
    drawLines=false,
    labelAngle,
    black)

  val yTics = YTics(scaledArea, Tics[Y].tics(minY, maxY), normalFontName, normalFontSize, true, black)

  val bars = slices.toStream.zipWithIndex.zip(colorStream).map({
    case ((s, i), color) => {
      val leftX = padding + (whiteSpace / 2d) + i * widthPerSlice
      val rightX = leftX + (widthPerSlice * barWidthPercent)
      Rectangle(scaledArea, Point2D(leftX, minY), Point2D(rightX, dataView.valueOf(data, s)), fillColor = Some(color))
    }
  })

}
