package com.gu.crossword.crosswords.models

import org.joda.time.{ Days, LocalDate, Weeks }

sealed trait CrosswordType extends Product with Serializable {
  def name: String
  def getNo(date: LocalDate): Option[Int]
}

case object Speedy extends CrosswordType {
  val name = "speedy"
  def getNo(date: LocalDate) = CrosswordTypeHelpers.getNoForWeeklyXword(1093, new LocalDate(2016, 9, 4), 7)(date)
}
case object Everyman extends CrosswordType {
  val name = "everyman"
  def getNo(date: LocalDate) = CrosswordTypeHelpers.getNoForWeeklyXword(3657, new LocalDate(2016, 11, 6), 7)(date)
}
case object Quiptic extends CrosswordType {
  val name = "quiptic"
  def getNo(date: LocalDate) = CrosswordTypeHelpers.getNoForWeeklyXword(877, new LocalDate(2016, 9, 5), 1)(date)
}
case object Prize extends CrosswordType {
  val name = "prize"
  def getNo(date: LocalDate) = {
    val basePubDate = new LocalDate(2016, 11, 12)
    val baseNo = 27040
    if (6 != date.getDayOfWeek) None
    else {
      val weekDiff = Weeks.weeksBetween(basePubDate, date).getWeeks
      Some(baseNo + weekDiff * 6)
    }
  }
}
case object Quick extends CrosswordType {
  val name = "quick"
  def getNo(date: LocalDate) = {
    val baseNo = 14460
    val basePubDate = new LocalDate(2016, 9, 12)
    if (date.getDayOfWeek == 7) None
    else {
      val dayDiff = Days.daysBetween(basePubDate, date).getDays
      val numberOfSundays = Math.floor(dayDiff / 7.0).toInt
      Some(baseNo + dayDiff - numberOfSundays)
    }
  }
}
case object Cryptic extends CrosswordType {
  val name = "cryptic"
  def getNo(date: LocalDate) = {
    val baseNo = 26981
    val basePubDate = new LocalDate(2016, 9, 5)
    if (date.getDayOfWeek == 6 || date.getDayOfWeek == 7) None
    else {
      val dayDiff = Days.daysBetween(basePubDate, date).getDays
      val numberOfSundays = Math.floor(dayDiff / 7.0).toInt
      Some(baseNo + dayDiff - numberOfSundays)
    }
  }
}

object CrosswordTypeHelpers {
  val allTypes = List(Speedy, Quick, Cryptic, Everyman, Quiptic, Prize)
  def getNoForWeeklyXword(baseNo: Int, basePubDate: LocalDate, publicationDayOfWeek: Int)(date: LocalDate): Option[Int] = {

    if (publicationDayOfWeek != date.getDayOfWeek) None
    else {
      val weekDiff = Weeks.weeksBetween(basePubDate, date).getWeeks
      Some(baseNo + weekDiff)
    }
  }
}