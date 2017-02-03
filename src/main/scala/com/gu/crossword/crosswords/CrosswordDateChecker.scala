package com.gu.crossword.crosswords

import com.gu.crossword.Config
import com.gu.crossword.services.SNS
import org.joda.time.LocalDate
import models._

import scala.concurrent.Future

object CrosswordDateChecker extends APIChecker {

  import scala.concurrent.ExecutionContext.Implicits.global

  def getAllCrosswordStatusesForDate(date: LocalDate)(config: Config): Future[List[CrosswordReadyStatus]] = {

    val crosswordStatuses = CrosswordTypeHelpers.allTypes.flatMap { cw =>
      cw.getNo(date).map { no =>
        val path = s"crosswords/${cw.name}/$no"
        val ready = checkIfCrosswordInApis(path)(config).map(r => CrosswordReadyStatus(cw.name, no, r.inCapiPreview))
        ready
      }
    }
    Future.sequence(crosswordStatuses)
  }

  def alertForBadCrosswords(date: LocalDate)(config: Config) = {

    println(s"Checking $date for crosswords that aren't ready")

    val invalidXWords = getAllCrosswordStatusesForDate(date)(config).map(_.filter(_.ready == false))
    invalidXWords.map(_.foreach(c => {
      println(s"Found invalid crossword - ${c.crosswordType} crossword ${c.number} for $date")
      val alertString = s"Warning - ${c.crosswordType} crossword ${c.number} for $date not ready! To view detailed status, see " +
        s"http://crossword-status-checker-prod.s3-website-eu-west-1.amazonaws.com/?type=${c.crosswordType}&id=${c.number}"
      SNS.publishMessage(alertString)(config)
    }))
  }
}