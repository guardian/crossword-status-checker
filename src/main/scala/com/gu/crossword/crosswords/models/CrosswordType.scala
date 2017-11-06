package com.gu.crossword.crosswords.models

import org.joda.time.{ Days, LocalDate, Weeks }

sealed trait CrosswordType extends Product with Serializable {
  def name: String
  def getNo(date: LocalDate): Option[Int]
  def getDate(no: Int): Option[LocalDate]
}

case object Speedy extends CrosswordType {
  val name = "speedy"
  def getNo(date: LocalDate) = CrosswordTypeHelpers.getNoForWeeklyXword(1093, new LocalDate(2016, 9, 4), 7)(date)
  def getDate(no: Int) = CrosswordTypeHelpers.getDateForWeeklyXWord(1093, new LocalDate(2016, 9, 4), 7)(no)
}
case object Everyman extends CrosswordType {
  val name = "everyman"
  def getNo(date: LocalDate) = CrosswordTypeHelpers.getNoForWeeklyXword(3706, new LocalDate(2017, 10, 22), 7)(date)
  def getDate(no: Int) = CrosswordTypeHelpers.getDateForWeeklyXWord(3706, new LocalDate(2017, 10, 22), 7)(no)
}
case object Quiptic extends CrosswordType {
  val name = "quiptic"
  def getNo(date: LocalDate) = CrosswordTypeHelpers.getNoForWeeklyXword(877, new LocalDate(2016, 9, 5), 1)(date)
  def getDate(no: Int) = CrosswordTypeHelpers.getDateForWeeklyXWord(877, new LocalDate(2016, 9, 5), 1)(no)
}
case object Weekend extends CrosswordType {
  val name = "weekend"
  def getNo(date: LocalDate) = CrosswordTypeHelpers.getNoForWeeklyXword(328, new LocalDate(2017, 4, 15), 6)(date)
  def getDate(no: Int) = CrosswordTypeHelpers.getDateForWeeklyXWord(328, new LocalDate(2017, 4, 15), 6)(no)
}
case object Prize extends CrosswordType {
  val name = "prize"
  val basePubDate = new LocalDate(2016, 11, 12)
  val baseNo = 27040

  def getNo(date: LocalDate) = {
    if (6 != date.getDayOfWeek) None
    else {
      val weekDiff = Weeks.weeksBetween(basePubDate, date).getWeeks
      Some(baseNo + weekDiff * 6)
    }
  }

  def getDate(no: Int) = {
    val noDiff = (no - baseNo)
    val date = basePubDate.plusWeeks(noDiff / 6)

    if (6 != date.getDayOfWeek) None
    else Some(date)
  }
}
case object Quick extends CrosswordType {
  val name = "quick"
  val baseNo = 14460
  val basePubDate = new LocalDate(2016, 9, 12)

  def getNo(date: LocalDate) = {
    if (date.getDayOfWeek == 7) None
    else {
      val dayDiff = Days.daysBetween(basePubDate, date).getDays
      val numberOfSundays = Math.floor(dayDiff / 7.0).toInt
      Some(baseNo + dayDiff - numberOfSundays)
    }
  }

  def getDate(no: Int) = {
    val noDiff = (no - baseNo)
    val numberOfSundays = Math.floor(noDiff / 7.0).toInt
    val date = basePubDate.plusDays(noDiff + numberOfSundays)
    if (date.getDayOfWeek == 7) None
    else Some(date)
  }
}
case object Cryptic extends CrosswordType {
  val name = "cryptic"
  val baseNo = 26981
  val basePubDate = new LocalDate(2016, 9, 5)

  def getNo(date: LocalDate) = {
    if (date.getDayOfWeek == 6 || date.getDayOfWeek == 7) None
    else {
      val dayDiff = Days.daysBetween(basePubDate, date).getDays
      val numberOfSundays = Math.floor(dayDiff / 7.0).toInt
      Some(baseNo + dayDiff - numberOfSundays)
    }
  }

  def getDate(no: Int) = {
    val noDiff = (no - baseNo)
    val numberOfSundays = Math.floor(noDiff / 7.0).toInt
    val date = basePubDate.plusDays(noDiff + numberOfSundays)

    if (date.getDayOfWeek == 7 || date.getDayOfWeek == 7) None
    else Some(date)
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
    val noDiff = (no - baseNo)
    val date = basePubDate.plusWeeks(noDiff)

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