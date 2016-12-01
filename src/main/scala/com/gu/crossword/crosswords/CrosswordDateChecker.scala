package com.gu.crossword.crosswords

import com.gu.crossword.Config
import com.gu.crossword.services.SNS
import org.joda.time.{ Days, LocalDate, Weeks }
import models._

import scala.concurrent.Future

object CrosswordDateChecker extends APIChecker {

  import scala.concurrent.ExecutionContext.Implicits.global

  def getAllCrosswordStatusesForDate(date: LocalDate)(config: Config): Future[List[(CrosswordType, Int, APIStatus)]] = {

    val crosswordStatuses = CrosswordTypeHelpers.allTypes.flatMap { cw =>
      cw.getNo(date).map { no =>
        val path = s"crosswords/${cw.name}/$no"
        val ready = checkIfCrosswordInApis(path)(config).map(r => (cw, no, r))
        ready
      }
    }
    Future.sequence(crosswordStatuses)
  }

  def alertForBadCrosswords(date: LocalDate)(config: Config) = {

    println(s"Checking $date for crosswords that aren't ready")

    val invalidXWords = getAllCrosswordStatusesForDate(date)(config).map(_.filter(_._3.inCapiPreview == false))
    invalidXWords.map(_.foreach(c => {
      println(s"Found invalid crossword - ${c._1.name} crossword ${c._2} for $date")
      val alertString = s"Warning - ${c._1.name} crossword ${c._2} for $date not ready!"
      SNS.publishMessage(alertString)(config)
    }))
  }
}