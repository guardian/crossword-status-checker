package com.gu.crossword.crosswords.models

import org.joda.time.{ Days, LocalDate, Weeks }

sealed trait CrosswordType extends Product with Serializable {
  def name: String
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
case object Prize extends CrosswordType {
  val name = "prize"
  val basePubDate = new LocalDate(2017, 10, 28)
  val baseNo = 27340

  def getNo(date: LocalDate) = {
    if (6 != date.getDayOfWeek) None
    else {
      val weekDiff = Weeks.weeksBetween(basePubDate, date).getWeeks
      Some(baseNo + weekDiff * 6)
    }
  }

  def getDate(no: Int) = {
    val dayDiff = no - baseNo
//    Divide by 6 as crossword number is only incremented 6 days a week
    val numberOfSundays = Math.floor(dayDiff / 6.0).toInt
    val date = basePubDate.plusDays(dayDiff + numberOfSundays)
    if (6 != date.getDayOfWeek) None
    else Some(date)
  }
}
case object Quick extends CrosswordType {
  val name = "quick"
  val baseNo = 14820
  val basePubDate = new LocalDate(2017, 11, 6)

  def getNo(date: LocalDate) = {
    if (date.getDayOfWeek == 7) None
    else {
      val dayDiff = Days.daysBetween(basePubDate, date).getDays
      val numberOfSundays = Math.floor(dayDiff / 7.0).toInt
      Some(baseNo + dayDiff - numberOfSundays)
    }
  }

  def getDate(no: Int) = {
    val dayDiff = no - baseNo
//    Divide by 6 as crossword number is only incremented 6 days a week
    val numberOfSundays = Math.floor(dayDiff / 6.0).toInt
    val date = basePubDate.plusDays(dayDiff + numberOfSundays)
    if (date.getDayOfWeek == 7) None
    else Some(date)
  }
}
case object Cryptic extends CrosswordType {
  val name = "cryptic"
  val baseNo = 27347
  val basePubDate = new LocalDate(2017, 11, 6)

  def getNo(date: LocalDate) = {
    if (date.getDayOfWeek == 6 || date.getDayOfWeek == 7) None
    else {
      val dayDiff = Days.daysBetween(basePubDate, date).getDays
      val numberOfSundays = Math.floor(dayDiff / 7.0).toInt
      Some(baseNo + dayDiff - numberOfSundays)
    }
  }
  def getDate(no: Int) = {
    val dayDiff = no - baseNo
//    Divide by 6 as crossword number is only incremented 6 days a week
    val numberOfSundays = Math.floor(dayDiff / 6.0).toInt
    val date = basePubDate.plusDays(dayDiff + numberOfSundays)

    if (date.getDayOfWeek == 6 || date.getDayOfWeek == 7) None
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