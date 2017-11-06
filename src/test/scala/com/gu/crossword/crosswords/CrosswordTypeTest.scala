package com.gu.crossword.crosswords

import com.gu.crossword.crosswords.models._
import org.joda.time.LocalDate
import org.scalatest.FunSuite

class CrosswordTypeTest extends FunSuite {

  test("test getNoForWeeklyXword") {
    assert(Speedy.getNo(new LocalDate(2017, 7, 23)) === Some(1138))
    assert(Speedy.getNo(new LocalDate(2016, 11, 4)) === None)
    assert(Quiptic.getNo(new LocalDate(2016, 11, 7)) === Some(886))
    assert(Everyman.getNo(new LocalDate(2017, 10, 15)) === Some(3705))
  }

  test("test getNoForPrizeXword") {
    assert(Prize.getNo(new LocalDate(2016, 11, 19)) === Some(27046))
    assert(Prize.getNo(new LocalDate(2016, 11, 18)) === None)
  }

  test("test getNoForQuickXword") {
    assert(Quick.getNo(new LocalDate(2016, 11, 5)) === Some(14507))
    assert(Quick.getNo(new LocalDate(2016, 11, 6)) === None)
  }

  test("test getNoForCrypticXword") {
    assert(Cryptic.getNo(new LocalDate(2017, 10, 10)) === Some(27324))
    assert(Cryptic.getNo(new LocalDate(2016, 12, 4)) === None)
  }

  test("test getDateForQuickXword") {
    assert(Quick.getDate(14803) === Some(new LocalDate(2017, 10, 17)))
  }

  test("test getDateForCripticXword") {
    assert(Cryptic.getDate(27326) === Some(new LocalDate(2017, 10, 12)))
    assert(Cryptic.getDate(27346) === None)
  }

  test("test getDateForWeeklyXword") {
    assert(Speedy.getDate(1151) === Some(new LocalDate(2017, 10, 22)))
    assert(Speedy.getDate(1153) === Some(new LocalDate(2017, 11, 5)))
    assert(Everyman.getDate(3696) === Some(new LocalDate(2017, 8, 13)))
    assert(Quiptic.getDate(923) === Some(new LocalDate(2017, 7, 24)))
  }

  test("test getDateForPrizeXword") {
    assert(Prize.getDate(27304) === Some(new LocalDate(2017, 9, 16)))
  }

}
