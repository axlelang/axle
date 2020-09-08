package axle.visualize

import cats.Order.catsKernelOrderingForOrder
import cats.Show
import cats.implicits._

import axle.algebra.Tics
import axle.visualize.Color.black
import axle.visualize.element.HorizontalLine
import axle.visualize.element.Rectangle
import axle.visualize.element.VerticalLine
import axle.visualize.element.XTics
import axle.visualize.element.YTics

case class BarChartView[C, Y, D, H](
  chart: BarChart[C, Y, D, H],
  data:  D) {

  import chart._

  val minX = 0d
  val maxX = 1d
  val yAxis = minX

  val slices = dataView.keys(data).toVector.sorted

  val padding = 0.05 // on each side
  val widthPerSlice = (1d - (2 * padding)) / slices.size
  val whiteSpace = widthPerSlice * (1d - barWidthPercent)

  val (dataMinY, dataMaxY) = dataView.yRange(data)

  val minY = List(xAxis.getOrElse(additiveMonoidY.zero), dataMinY).min
  val maxY = List(xAxis.getOrElse(additiveMonoidY.zero), dataMaxY).max

  implicit val ddls = axle.algebra.LengthSpace.doubleDoubleLengthSpace

  val scaledArea = ScaledArea2D(
    border.toDouble, (if (drawKey) width - (keyWidth + keyLeftPadding) else width) - border.toDouble,
    border.toDouble, height - (if (labelAngle.isDefined) border.toDouble else 5d),
    minX, maxX,
    minY, maxY)

  val vLine = VerticalLine(scaledArea, yAxis, black)
  val hLine = HorizontalLine(scaledArea, xAxis.getOrElse(additiveMonoidY.zero), black)

  val gTics = XTics(
    scaledArea,
    slices.to(LazyList).zipWithIndex.map({ case (c, i) => (padding + (i + 0.5) * widthPerSlice, showC.show(c)) }).toList,
    normalFontName,
    normalFontSize.toDouble,
    bold = true,
    drawLines = false,
    labelAngle,
    black)

  val yTics = YTics(scaledArea, Tics[Y].tics(minY, maxY), normalFontName, normalFontSize.toDouble, true, black)

  val bars = slices.to(LazyList).zipWithIndex.map({
    case (c, i) => {
      val y = dataView.valueOf(data, c)
      val color = colorOf(c)
      val leftX = padding + (whiteSpace / 2d) + i * widthPerSlice
      val rightX = leftX + (widthPerSlice * barWidthPercent)
      val y0 = additiveMonoidY.zero

      val (ll, ur) =
        if (y >= y0) {
          (Point2D(leftX, y0), Point2D(rightX, y))
        } else {
          (Point2D(leftX, y), Point2D(rightX, y0))
        }

      val baseRect = Rectangle(scaledArea, ll, ur, fillColor = Some(color), id = Some(Show[Int].show(i)))

      val hovered =
        hoverOf(c) map {
          case (hover) =>
            baseRect.copy(hoverText = Some(showH.show(hover)))
        } getOrElse { baseRect }

      linkOf(c) map {
        case (url, color) =>
          hovered.copy(link = Some((url, color)))
      } getOrElse { hovered }

    }
  })

}
