package com.gu.crossword.crosswords.models

import org.joda.time.{ Days, LocalDate, Weeks }

import scala.annotation.tailrec

sealed trait CrosswordType extends Product with Serializable {
  def name: String

  def baseNo: Int
  def basePubDate: LocalDate

  def getNo(date: LocalDate): Option[Int]
  def getDate(no: Int): Option[LocalDate]
}

case object Speedy extends CrosswordType {
  val name = "speedy"
  val baseNo = 1153
  val basePubDate = new LocalDate(2017, 11, 5)
  def getNo(date: LocalDate) = CrosswordTypeHelpers.getNoForWeeklyXword(baseNo, basePubDate, 7)(date)
  def getDate(no: Int) = CrosswordTypeHelpers.getDateForWeeklyXWord(baseNo, basePubDate, 7)(no)
}
case object Everyman extends CrosswordType {
  val name = "everyman"
  val baseNo = 3708
  val basePubDate = new LocalDate(2017, 11, 5)
  def getNo(date: LocalDate) = CrosswordTypeHelpers.getNoForWeeklyXword(baseNo, basePubDate, 7)(date)
  def getDate(no: Int) = CrosswordTypeHelpers.getDateForWeeklyXWord(baseNo, basePubDate, 7)(no)
}
case object Quiptic extends CrosswordType {
  val name = "quiptic"
  val baseNo = 938
  val basePubDate = new LocalDate(2017, 11, 6)
  def getNo(date: LocalDate) = CrosswordTypeHelpers.getNoForWeeklyXword(baseNo, basePubDate, 1)(date)
  def getDate(no: Int) = CrosswordTypeHelpers.getDateForWeeklyXWord(baseNo, basePubDate, 1)(no)
}
case object Weekend extends CrosswordType {
  val name = "weekend"
  val baseNo = 357
  val basePubDate = new LocalDate(2017, 11, 4)
  def getNo(date: LocalDate) = CrosswordTypeHelpers.getNoForWeeklyXword(baseNo, basePubDate, 6)(date)
  def getDate(no: Int) = CrosswordTypeHelpers.getDateForWeeklyXWord(baseNo, basePubDate, 6)(no)
}
case object Prize extends EveryDayExceptSundayCrossword {
  val name = "prize"
  val basePubDate = new LocalDate(2017, 10, 28)
  val baseNo = 27340

  final override def validate(date: LocalDate): Boolean = {
    date.getDayOfWeek == 6 // only saturdays
  }
}
case object Quick extends EveryDayExceptSundayCrossword {
  val name = "quick"
  val baseNo = 14820
  val basePubDate = new LocalDate(2017, 11, 6)

  override def validate(date: LocalDate): Boolean = {
    date.getDayOfWeek != 7 // not sundays
  }
}
case object Cryptic extends EveryDayExceptSundayCrossword {
  val name = "cryptic"
  val baseNo = 27347
  val basePubDate = new LocalDate(2017, 11, 6)

  override def validate(date: LocalDate): Boolean = {
    date.getDayOfWeek < 6 // every weekday
  }
}

trait EveryDayExceptSundayCrossword extends CrosswordType {
  def validate(date: LocalDate): Boolean

  final def getNo(date: LocalDate): Option[Int] = {
    if (validate(date)) {
      search(Right(date), baseNo, basePubDate).map { case (no, _) => no }
    } else {
      None
    }
  }

  final def getDate(no: Int): Option[LocalDate] = {
    search(Left(no), baseNo, basePubDate).collect {
      case (_, date) if validate(date) => date
    }
  }

  @tailrec
  private def search(target: Either[Int, LocalDate], number: Int, date: LocalDate): Option[(Int, LocalDate)] = {
    val isSunday = date.getDayOfWeek == 7
    val isChristmas = date.getDayOfMonth == 25 && date.getMonthOfYear == 12

    target match {
      case Left(t) if number > t =>
        None

      case Right(t) if date.compareTo(t) > 0 =>
        None

      case _ =>
        val hitTarget = target == Left(number) || target == Right(date)

        if (hitTarget && !isSunday && !isChristmas) {
          Some((number, date))
        } else if (isSunday || isChristmas) {
          search(target, number, date.plusDays(1))
        } else {
          search(target, number + 1, date.plusDays(1))
        }
    }
  }
}

object CrosswordTypeHelpers {
  val allTypes = List(Speedy, Quick, Cryptic, Everyman, Quiptic, Prize, Weekend)
  def getNoForWeeklyXword(baseNo: Int, basePubDate: LocalDate, publicationDayOfWeek: Int)(date: LocalDate): Option[Int] = {

    if (publicationDayOfWeek != date.getDayOfWeek) None
    else {
      val weekDiff = Weeks.weeksBetween(basePubDate, date).getWeeks
      Some(baseNo + weekDiff)
    }
  }

  def getDateForWeeklyXWord(baseNo: Int, basePubDate: LocalDate, publicationDayOfWeek: Int)(no: Int): Option[LocalDate] = {
    val weekDiff = no - baseNo
    val date = basePubDate.plusWeeks(weekDiff)

    if (publicationDayOfWeek != date.getDayOfWeek) None
    else Some(date)
  }

  def getXWordType(name: String) = {
    name match {
      case "speedy" => Speedy
      case "quick" => Quick
      case "cryptic" => Cryptic
      case "everyman" => Everyman
      case "quiptic" => Quiptic
      case "prize" => Prize
      case "weekend" => Weekend
    }
  }
}