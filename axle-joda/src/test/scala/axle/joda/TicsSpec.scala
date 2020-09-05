package axle.joda

import axle.algebra.Tics
import org.scalatest.funsuite._
import org.scalatest.matchers.should.Matchers

import org.joda.time.DateTime
import org.joda.time.DateTimeZone

class TicsSpec extends AnyFunSuite with Matchers {

  val pacificTimeZone = DateTimeZone.forID("America/Los_Angeles")

  def ticsInPacific(dtl: (DateTime, String)): (DateTime, String) = {
    (dtl._1.withZone(pacificTimeZone), dtl._2)
  }

  val start = new DateTime("2016-01-04T12:10:05.000-08:00").withZone(pacificTimeZone)

  test("Tics[DateTime] cover five seconds") {

    Tics[DateTime].tics(start, start.plusSeconds(5)).map(ticsInPacific) should be(List(
      (new DateTime("2016-01-04T12:10:06.000-08:00"), "10:06"),
      (new DateTime("2016-01-04T12:10:07.000-08:00"), "10:07"),
      (new DateTime("2016-01-04T12:10:08.000-08:00"), "10:08"),
      (new DateTime("2016-01-04T12:10:09.000-08:00"), "10:09"),
      (new DateTime("2016-01-04T12:10:10.000-08:00"), "10:10")).map(ticsInPacific))
  }

  test("Tics[DateTime] cover thirty seconds") {

    Tics[DateTime].tics(start, start.plusSeconds(30)).map(ticsInPacific) should be(List(
      (new DateTime("2016-01-04T12:10:15.000-08:00"), "10:15"),
      (new DateTime("2016-01-04T12:10:25.000-08:00"), "10:25"),
      (new DateTime("2016-01-04T12:10:35.000-08:00"), "10:35")).map(ticsInPacific))
  }

  test("Tics[DateTime] cover five minutes") {

    Tics[DateTime].tics(start, start.plusMinutes(5)).map(ticsInPacific) should be(List(
      (new DateTime("2016-01-04T12:11:05.000-08:00"), "12:11"),
      (new DateTime("2016-01-04T12:12:05.000-08:00"), "12:12"),
      (new DateTime("2016-01-04T12:13:05.000-08:00"), "12:13"),
      (new DateTime("2016-01-04T12:14:05.000-08:00"), "12:14"),
      (new DateTime("2016-01-04T12:15:05.000-08:00"), "12:15")).map(ticsInPacific))
  }

  test("Tics[DateTime] cover five hours") {

    Tics[DateTime].tics(start, start.plusHours(5)).map(ticsInPacific) should be(List(
      (new DateTime("2016-01-04T13:10:05.000-08:00"), "04 01:10"),
      (new DateTime("2016-01-04T14:10:05.000-08:00"), "04 02:10"),
      (new DateTime("2016-01-04T15:10:05.000-08:00"), "04 03:10"),
      (new DateTime("2016-01-04T16:10:05.000-08:00"), "04 04:10"),
      (new DateTime("2016-01-04T17:10:05.000-08:00"), "04 05:10")).map(ticsInPacific))
  }

  test("Tics[DateTime] cover twelve hours") {

    Tics[DateTime].tics(start, start.plusHours(12)).map(ticsInPacific) should be(List(
      (new DateTime("2016-01-04T14:10:05.000-08:00"), "04 02:10"),
      (new DateTime("2016-01-04T16:10:05.000-08:00"), "04 04:10"),
      (new DateTime("2016-01-04T18:10:05.000-08:00"), "04 06:10"),
      (new DateTime("2016-01-04T20:10:05.000-08:00"), "04 08:10"),
      (new DateTime("2016-01-04T22:10:05.000-08:00"), "04 10:10"),
      (new DateTime("2016-01-05T00:10:05.000-08:00"), "05 12:10")).map(ticsInPacific))
  }

  test("Tics[DateTime] cover five days") {

    Tics[DateTime].tics(start, start.plusDays(5)).map(ticsInPacific) should be(List(
      (new DateTime("2016-01-05T12:10:05.000-08:00"), "01/05 12"),
      (new DateTime("2016-01-06T12:10:05.000-08:00"), "01/06 12"),
      (new DateTime("2016-01-07T12:10:05.000-08:00"), "01/07 12"),
      (new DateTime("2016-01-08T12:10:05.000-08:00"), "01/08 12"),
      (new DateTime("2016-01-09T12:10:05.000-08:00"), "01/09 12")).map(ticsInPacific))
  }

  test("Tics[DateTime] cover a week") {

    Tics[DateTime].tics(start, start.plusWeeks(1)).map(ticsInPacific) should be(List(
      (new DateTime("2016-01-05T12:10:05.000-08:00"), "01/05 12"),
      (new DateTime("2016-01-06T12:10:05.000-08:00"), "01/06 12"),
      (new DateTime("2016-01-07T12:10:05.000-08:00"), "01/07 12"),
      (new DateTime("2016-01-08T12:10:05.000-08:00"), "01/08 12"),
      (new DateTime("2016-01-09T12:10:05.000-08:00"), "01/09 12"),
      (new DateTime("2016-01-10T12:10:05.000-08:00"), "01/10 12"),
      (new DateTime("2016-01-11T12:10:05.000-08:00"), "01/11 12")).map(ticsInPacific))
  }

  test("Tics[DateTime] cover four weeks") {

    Tics[DateTime].tics(start, start.plusWeeks(4)).map(ticsInPacific) should be(List(
      (new DateTime("2016-01-11T12:10:05.000-08:00"), "01/11"),
      (new DateTime("2016-01-18T12:10:05.000-08:00"), "01/18"),
      (new DateTime("2016-01-25T12:10:05.000-08:00"), "01/25"),
      (new DateTime("2016-02-01T12:10:05.000-08:00"), "02/01")).map(ticsInPacific))
  }

  test("Tics[DateTime] cover a month") {

    Tics[DateTime].tics(start, start.plusMonths(1)).map(ticsInPacific) should be(List(
      (new DateTime("2016-01-11T12:10:05.000-08:00"), "01/11"),
      (new DateTime("2016-01-18T12:10:05.000-08:00"), "01/18"),
      (new DateTime("2016-01-25T12:10:05.000-08:00"), "01/25"),
      (new DateTime("2016-02-01T12:10:05.000-08:00"), "02/01")).map(ticsInPacific))
  }

  test("Tics[DateTime] cover a year") {

    Tics[DateTime].tics(start, start.plusYears(1)).map(ticsInPacific) should be(List(
      (new DateTime("2016-02-04T12:10:05.000-08:00"), "02/04 16"),
      (new DateTime("2016-03-04T12:10:05.000-08:00"), "03/04 16"),
      (new DateTime("2016-04-04T12:10:05.000-07:00"), "04/04 16"),
      (new DateTime("2016-05-04T12:10:05.000-07:00"), "05/04 16"),
      (new DateTime("2016-06-04T12:10:05.000-07:00"), "06/04 16"),
      (new DateTime("2016-07-04T12:10:05.000-07:00"), "07/04 16"),
      (new DateTime("2016-08-04T12:10:05.000-07:00"), "08/04 16"),
      (new DateTime("2016-09-04T12:10:05.000-07:00"), "09/04 16"),
      (new DateTime("2016-10-04T12:10:05.000-07:00"), "10/04 16"),
      (new DateTime("2016-11-04T12:10:05.000-07:00"), "11/04 16"),
      (new DateTime("2016-12-04T12:10:05.000-08:00"), "12/04 16"),
      (new DateTime("2017-01-04T12:10:05.000-08:00"), "01/04 17")).map(ticsInPacific))
  }

  test("Tics[DateTime] cover three years") {

    Tics[DateTime].tics(start, start.plusYears(3)).map(ticsInPacific) should be(List(
      (new DateTime("2016-07-04T12:10:05.000-07:00"), "07/04 16"),
      (new DateTime("2017-01-04T12:10:05.000-08:00"), "01/04 17"),
      (new DateTime("2017-07-04T12:10:05.000-07:00"), "07/04 17"),
      (new DateTime("2018-01-04T12:10:05.000-08:00"), "01/04 18"),
      (new DateTime("2018-07-04T12:10:05.000-07:00"), "07/04 18"),
      (new DateTime("2019-01-04T12:10:05.000-08:00"), "01/04 19")).map(ticsInPacific))
  }

}
