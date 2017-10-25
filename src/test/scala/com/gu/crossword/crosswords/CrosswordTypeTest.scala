package com.gu.crossword.crosswords

import com.gu.crossword.crosswords.models._
import org.joda.time.LocalDate
import org.scalatest.FunSuite

class CrosswordTypeTest extends FunSuite {

  test("testGetNoForWeeklyXword") {
    assert(Speedy.getNo(new LocalDate(2016, 11, 6)) === Some(1102))
    assert(Speedy.getNo(new LocalDate(2016, 11, 4)) === None)
    assert(Quiptic.getNo(new LocalDate(2016, 11, 7)) === Some(886))
    assert(Everyman.getNo(new LocalDate(2017, 10, 15)) === Some(3705))
    assert(Prize.getNo(new LocalDate(2016, 11, 19)) === Some(27046))
    assert(Prize.getNo(new LocalDate(2016, 11, 18)) === None)
  }

  test("test getNoForQuickXword") {
    assert(Quick.getNo(new LocalDate(2016, 11, 5)) === Some(14507))
    assert(Quick.getNo(new LocalDate(2016, 11, 6)) === None)
  }

  test("test getNoForCrypticXword") {
    assert(Cryptic.getNo(new LocalDate(2016, 12, 1)) === Some(27056))
    assert(Cryptic.getNo(new LocalDate(2016, 12, 4)) === None)
  }

}
