package com.gu.crossword.crosswords

import com.gu.crossword.crosswords.models._
import org.joda.time.LocalDate
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

class CrosswordTypeTest extends AnyFunSuite with Matchers {
  test("test getNoForWeeklyXword") {
    assert(Speedy.getNo(new LocalDate(2017, 7, 23)) === Some(1138))
    assert(Speedy.getNo(new LocalDate(2016, 11, 4)) === None)
    assert(Speedy.getNo(new LocalDate(2023, 6, 18)) === Some(1445))
    assert(Quiptic.getNo(new LocalDate(2016, 11, 7)) === Some(886))
    assert(Quiptic.getNo(new LocalDate(2017, 12, 25)) === Some(945)) // quiptic publishes/published on xmas
    assert(Quiptic.getNo(new LocalDate(2023, 6, 19)) === Some(1231))
    assert(Everyman.getNo(new LocalDate(2017, 10, 15)) === Some(3705))
    assert(Everyman.getNo(new LocalDate(2023, 6, 18)) === Some(4000))

    assert(Weekend.getNo(new LocalDate(2017, 11, 4)) === Some(357))
    assert(Weekend.getNo(new LocalDate(2021, 12, 18)) === Some(572))
    assert(Weekend.getNo(new LocalDate(2021, 12, 25)) === None)
    assert(Weekend.getNo(new LocalDate(2022, 1, 1)) === Some(573))
    assert(Weekend.getNo(new LocalDate(2023, 6, 24)) === Some(650))
  }

  test("test getNoForPrizeXword") {
    assert(Prize.getNo(new LocalDate(2017, 12, 23)) === Some(27388))
    assert(Prize.getNo(new LocalDate(2017, 12, 22)) === None)
  }

  test("test getNoForQuickXword") {
    assert(Quick.getNo(new LocalDate(2017, 11, 8)) === Some(14822))
    assert(Quick.getNo(new LocalDate(2017, 11, 12)) === None)
  }

  test("test getNoForCrypticXword") {
    assert(Cryptic.getNo(new LocalDate(2017, 11, 6)) === Some(27347))
    assert(Cryptic.getNo(new LocalDate(2017, 11, 11)) === None)
  }

  test("test getDateForQuickXword") {
    assert(Quick.getDate(14826) === Some(new LocalDate(2017, 11, 13)))
  }

  test("test getDateForCrypticXword") {
    assert(Cryptic.getDate(27347) === Some(new LocalDate(2017, 11, 6)))
    assert(Cryptic.getDate(27352) === None)
  }

  test("test getDateForWeeklyXword") {
    assert(Speedy.getDate(1151) === Some(new LocalDate(2017, 10, 22)))
    assert(Speedy.getDate(1153) === Some(new LocalDate(2017, 11, 5)))
    assert(Speedy.getDate(1445) === Some(new LocalDate(2023, 6, 18)))
    assert(Everyman.getDate(3696) === Some(new LocalDate(2017, 8, 13)))
    assert(Everyman.getDate(4000) === Some(new LocalDate(2023, 6, 18)))
    assert(Quiptic.getDate(923) === Some(new LocalDate(2017, 7, 24)))
    assert(Quiptic.getDate(945) === Some(new LocalDate(2017, 12, 25)))
    assert(Quiptic.getDate(1231) === Some(new LocalDate(2023, 6, 19)))

    assert(Weekend.getDate(357) === Some(new LocalDate(2017, 11, 4)))
    assert(Weekend.getDate(572) === Some(new LocalDate(2021, 12, 18)))
    assert(Weekend.getDate(573) === Some(new LocalDate(2022, 1, 1)))
    assert(Weekend.getDate(650) === Some(new LocalDate(2023, 6, 24)))
  }

  test("test getDateForPrizeXword") {
    assert(Prize.getDate(27388) === Some(new LocalDate(2017, 12, 23)))
  }

  test("test christmas") {
    Cryptic.getNo(new LocalDate(2017, 12, 25)) mustBe empty
    Prize.getNo(new LocalDate(2017, 12, 25)) mustBe empty
    Quiptic.getNo(new LocalDate(2017, 12, 25)) must contain(945)
  }

  test("prize and cryptic after christmas") {
    val saturday = new LocalDate(2017, 12, 30)
    val monday = new LocalDate(2018, 1, 1)

    Prize.getDate(27393) must contain(saturday)
    Prize.getNo(saturday) must contain(27393)
    Cryptic.getDate(27393) mustBe empty
    Cryptic.getNo(saturday) mustBe empty

    Prize.getDate(27394) mustBe empty
    Prize.getNo(monday) mustBe empty
    Cryptic.getDate(27394) must contain(monday)
    Cryptic.getNo(monday) must contain(27394)
  }

  test("test christmas 2017") {
    // 'twas the night before the night before christmas...
    // and the Guardian elves saw it was a Saturday and published a Prize crossword instead of a Cryptic one
    Prize.getDate(27388) must contain(new LocalDate(2017, 12, 23))

    // 'twas actually the night before christmas and it was a Sunday...
    // so nothing was stirring, not even a mouse and certainly not any crosswords
    Cryptic.getNo(new LocalDate(2017, 12, 24)) mustBe empty
    Prize.getNo(new LocalDate(2017, 12, 24)) mustBe empty

    // Noddy says it's Christmas and The Guardian is not publishing a paper
    // so no cryptic or prize
    Cryptic.getNo(new LocalDate(2017, 12, 25)) mustBe empty
    Prize.getNo(new LocalDate(2017, 12, 25)) mustBe empty

    // then we all forgot about how crossword status checker works until next year!
    Cryptic.getDate(27389) must contain(new LocalDate(2017, 12, 26)) // Tuesday
    Cryptic.getDate(27390) must contain(new LocalDate(2017, 12, 27)) // Wednesday
    Cryptic.getDate(27391) must contain(new LocalDate(2017, 12, 28)) // Thursday
    Cryptic.getDate(27392) must contain(new LocalDate(2017, 12, 29)) // Friday

    // Saturday (Prize)
    Prize.getDate(27393) must contain(new LocalDate(2017, 12, 30))
    Cryptic.getDate(27393) mustBe empty
  }

  test("Quick 2018-01-06") {
    Quick.getDate(14872) must contain(new LocalDate(2018, 1, 6))
    Quick.getNo(new LocalDate(2018, 1, 6)) must contain(14872)
  }

  test("test base case") {
    CrosswordTypeHelpers.allTypes.foreach { crossword =>
      crossword.getDate(crossword.baseNo) must contain(crossword.basePubDate)
      crossword.getNo(crossword.basePubDate) must contain(crossword.baseNo)
    }
  }
}
