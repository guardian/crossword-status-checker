package com.gu.crossword.crosswords

import com.gu.crossword.Config
import com.gu.crossword.services.{Constants, SNS}
import org.joda.time.{LocalDate, Period}
import models._

import scala.concurrent.Future

object CrosswordDateChecker {

  import scala.concurrent.ExecutionContext.Implicits.global

  def generateListOfNextNDays(n: Int): List[LocalDate] = {
    val today = new LocalDate()
    val oneDay = Period.days(1)
    (1 to n).toList.map(
      today.withPeriodAdded(oneDay, _)
    )
  }

  def getAllCrosswordStatusesForDate(
      date: LocalDate
  )(
      config: Config,
      requestBuilder: RequestBuilder
  ): Future[List[CrosswordReadyStatus]] = {

    println(s"Checking status of crosswords on $date")

    val crosswordStatuses = CrosswordTypeHelpers.allTypes.flatMap { cw =>
      cw.getNo(date).map { no =>
        val path = s"crosswords/${cw.name}/$no"
        val ready = APIChecker
          .checkIfCrosswordInApis(path)(config, requestBuilder)
          .map(r => CrosswordReadyStatus(cw.name, no, r.inCapiPreview, date))
        ready
      }
    }
    Future.sequence(crosswordStatuses)
  }

  def checkNextNDays(
      noDaysToCheck: Int
  )(config: Config): Future[List[List[CrosswordReadyStatus]]] = {
    val daysToCheck = generateListOfNextNDays(noDaysToCheck)
    println(
      s"Checking the next $noDaysToCheck days for crosswords which aren't ready: ${daysToCheck.mkString(", ")}"
    )

    val requestBuilder =
      new RequestBuilderWithSigner(
        config.capiPreviewRole,
        Constants.awsRegion.toString
      )

    // get statuses
    Future.sequence(
      daysToCheck.map(d =>
        getAllCrosswordStatusesForDate(d)(config, requestBuilder)
      )
    )
  }

  def alertForBadCrosswords(
      statuses: List[CrosswordReadyStatus]
  )(config: Config) = {

    val invalidXWords = statuses.filter(_.ready == false)
    invalidXWords.foreach(c => {
      println(
        s"Found invalid crossword - ${c.crosswordType} crossword ${c.number} for ${c.date}"
      )
      val alertString =
        s"Warning - ${c.crosswordType} crossword ${c.number} for ${c.date} not ready! To view detailed status, see " +
          s"http://crossword-status-checker-prod.s3-website-eu-west-1.amazonaws.com/?type=${c.crosswordType}&id=${c.number}"
      SNS.publishMessage(alertString)(config)
    })
  }
}
