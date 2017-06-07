package com.gu.crossword

import org.joda.time.{ LocalDate }
import java.util.{ Map => JMap }

import com.amazonaws.services.lambda.runtime.{ Context, RequestHandler }
import com.gu.crossword.crosswords.models.CrosswordStatus
import com.gu.crossword.crosswords.{ APIChecker, CrosswordStore }
import com.gu.crossword.crosswords.CrosswordDateChecker._
import org.json4s._
import org.json4s.native.Serialization.write

import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global

class Lambda
    extends RequestHandler[JMap[String, Object], String]
    with APIChecker
    with CrosswordStore {

  override def handleRequest(event: JMap[String, Object], context: Context): String = {

    implicit val config = new Config(context)

    if (event.containsKey("type") && event.containsKey("id")) {
      val crosswordType = event.get("type").toString
      val crosswordId = event.get("id").toString

      println(s"Getting status of $crosswordType $crosswordId")

      val s3Status = checkCrosswordS3Status(crosswordId, crosswordType)

      val path = s"crosswords/$crosswordType/$crosswordId"
      val apiStatus = checkIfCrosswordInApis(path)(config)

      val status = CrosswordStatus(s3Status, Await.result(apiStatus, 10 seconds))

      val statusJson = CrosswordStatus.toJson(status)
      statusJson
    } else if (event.containsKey("checkNextNDays")) {
      val noDaysToCheck = event.get("checkNextNDays").toString.toInt

      if (noDaysToCheck > 10) {
        println(s"User requested $noDaysToCheck. Max: 10")
        "Can check a maximum of 10 days"
      } else {
        val daysToCheck = generateListOfNextNDays(noDaysToCheck)
        println(s"Checking the next $noDaysToCheck days for crosswords which aren't ready: ${daysToCheck.mkString(", ")}")

        // get statuses
        val statuses = Await.result(Future.sequence(daysToCheck.map(d => getAllCrosswordStatusesForDate(d)(config))), 10 seconds)

        // alert
        alertForBadCrosswords(statuses.flatten)(config)

        s"checked $noDaysToCheck days"
      }
    } else if (event.containsKey("dateToCheck")) {
      val dateToCheck = event.get("dateToCheck").toString
      val d = LocalDate.parse(dateToCheck)

      val dateStatusFt = getAllCrosswordStatusesForDate(d)(config)
      val dateStatus = Await.result(dateStatusFt, 10 seconds)

      val jsonStatus = write(dateStatus)(DefaultFormats)
      println(s"JSON status of crosswords for ${d.toString}: $jsonStatus")
      jsonStatus
    } else {
      "Crossword id and type or type and check query must be provided"
    }
  }

}