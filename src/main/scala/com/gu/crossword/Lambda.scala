package com.gu.crossword

import org.joda.time.{ LocalDate, Period, ReadablePeriod }
import java.util.{ Map => JMap }

import com.amazonaws.services.lambda.runtime.{ Context, RequestHandler }
import com.gu.crossword.crosswords.models.CrosswordStatus
import com.gu.crossword.crosswords.{ APIChecker, CrosswordDateChecker, CrosswordStore }

import scala.concurrent.duration._
import scala.concurrent.Await

class Lambda
    extends RequestHandler[JMap[String, Object], String]
    with APIChecker
    with CrosswordStore {

  override def handleRequest(event: JMap[String, Object], context: Context): String = {

    implicit val config = new Config(context)

    println(event)

    if (event.containsKey("type") && event.containsKey("id")) {
      val crosswordType = event.get("type").toString
      val crosswordId = event.get("id").toString

      println(s"Getting status of $crosswordType $crosswordId")

      val s3Status = checkCrosswordS3Status(crosswordId, crosswordType)
      val path = s"crosswords/$crosswordType/$crosswordId"
      val apiStatus = checkIfCrosswordInApis(path)(config)

      import scala.language.postfixOps
      val status = CrosswordStatus(s3Status, Await.result(apiStatus, 10 seconds))

      val statusJson = CrosswordStatus.toJson(status)
      println(statusJson)
      statusJson
    } else if (event.containsKey("checkNext3Days")) {

      println("Checking the next 3 days for crosswords which aren't ready.")

      val today = new LocalDate()
      val oneDay = Period.days(1)
      val daysToCheck = List(
        today.withPeriodAdded(oneDay, 1),
        today.withPeriodAdded(oneDay, 2),
        today.withPeriodAdded(oneDay, 3)
      )

      println(s"Checking days ${daysToCheck.mkString(", ")}")
      daysToCheck.foreach(d => CrosswordDateChecker.alertForBadCrosswords(d)(config))

      "checked 3 days"

    } else {
      "Crossword id and type or type and check query must be provided"
    }

  }

}