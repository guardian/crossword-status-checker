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

case object Speedy extends WeeklyCrossword {
  val name = "speedy"
  val baseNo = 1153
  val basePubDate = new LocalDate(2017, 11, 5)
  val publicationDayOfWeek: Int = 7
}
case object Everyman extends WeeklyCrossword {
  val name = "everyman"
  val baseNo = 3708
  val basePubDate = new LocalDate(2017, 11, 5)
  val publicationDayOfWeek: Int = 7
}
case object Quiptic extends WeeklyCrossword {
  val name = "quiptic"
  val baseNo = 938
  val basePubDate = new LocalDate(2017, 11, 6)
  val publicationDayOfWeek: Int = 1
  override val publishesOnChristmas: Boolean = true
}
case object Weekend extends WeeklyCrossword {
  val name = "weekend"
  val baseNo = 357
  val basePubDate = new LocalDate(2017, 11, 4)
  val publicationDayOfWeek: Int = 6
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

trait WeeklyCrossword extends CrosswordType {
  val publicationDayOfWeek: Int // matching the joda numbers for dayOfWeek - Mon=1, Tue=2, Sun=7 etc.
  val publishesOnChristmas: Boolean = false

  private def isBetween(date: LocalDate, rangestart: LocalDate, rangeend: LocalDate): Boolean = {
    (date.isAfter(rangestart) && date.isBefore(rangeend)) || (date.isAfter(rangeend) && date.isBefore(rangestart))
  }

  final def getNo(date: LocalDate): Option[Int] = {
    val weekDiff = Weeks.weeksBetween(basePubDate, date).getWeeks
    if (publicationDayOfWeek != date.getDayOfWeek) None
    else if (publishesOnChristmas) Some(baseNo + weekDiff)
    else if (!publishesOnChristmas && date.getMonthOfYear == 12 && date.getDayOfMonth == 25) None
    else {
      val christmasses = (for {
        year <- basePubDate.getYear to date.getYear
        xmasDate = new LocalDate(year, 12, 25)
        if xmasDate.getDayOfWeek == publicationDayOfWeek && isBetween(xmasDate, basePubDate, date)
      } yield xmasDate).size
      Some(baseNo + weekDiff - christmasses)
    }
  }

  @tailrec
  private def searchForDate(date: LocalDate, weekDiff: Int, direction: Int): LocalDate = {
    val canPublishOnDate = publishesOnChristmas || !(date.getMonthOfYear == 12 && date.getDayOfMonth == 25)
    if (weekDiff == 0 && canPublishOnDate) date
    else if (!canPublishOnDate) searchForDate(date.plusWeeks(direction), weekDiff, direction)
    else searchForDate(date.plusWeeks(direction), weekDiff - direction, direction)
  }

  final def getDate(number: Int): Option[LocalDate] = {
    if (number == baseNo) {
      Some(basePubDate)
    } else {
      val weekDiff = number - baseNo
      val direction = if (weekDiff > 0) 1 else -1
      val date = searchForDate(basePubDate, weekDiff, direction)
      if (date.getDayOfWeek == publicationDayOfWeek) {
        Some(date)
      } else {
        None
      }
    }
  }
}


object CrosswordTypeHelpers {
  val allTypes = List(Speedy, Quick, Cryptic, Everyman, Quiptic, Prize, Weekend)

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
