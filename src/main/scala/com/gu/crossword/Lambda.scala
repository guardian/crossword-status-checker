package com.gu.crossword

import org.joda.time.LocalDate

import java.util.{Map => JMap}
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.gu.crossword.crosswords.models.CrosswordStatus
import com.gu.crossword.crosswords.{
  APIChecker,
  CrosswordStore,
  RequestBuilderWithSigner
}
import com.gu.crossword.crosswords.CrosswordDateChecker._
import com.gu.crossword.services.Constants
import org.json4s._
import org.json4s.native.Serialization.write

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global

class Lambda
    extends RequestHandler[JMap[String, Object], String]
    with CrosswordStore {

  override def handleRequest(
      event: JMap[String, Object],
      context: Context
  ): String = {

    implicit val config = Config.fromContext(context)

    val requestBuilder =
      new RequestBuilderWithSigner(
        config.capiPreviewRole,
        Constants.awsRegion.toString
      )

    if (event.containsKey("type") && event.containsKey("id")) {
      val crosswordType = event.get("type").toString
      val crosswordId = event.get("id").toString

      println(s"Getting status of $crosswordType $crosswordId")

      val s3Status = checkCrosswordS3Status(crosswordId, crosswordType)

      val path = s"crosswords/$crosswordType/$crosswordId"
      val apiStatus =
        APIChecker.checkIfCrosswordInApis(path)(config, requestBuilder)

      val status =
        CrosswordStatus(s3Status, Await.result(apiStatus, 10 seconds))

      val statusJson = CrosswordStatus.toJson(status)
      statusJson
    } else if (event.containsKey("checkNextNDays")) {
      val noDaysToCheck = event.get("checkNextNDays").toString.toInt

      if (noDaysToCheck > 10) {
        println(s"User requested $noDaysToCheck. Max: 10")
        "Can check a maximum of 10 days"
      } else {
        val statuses = Await
          .result(checkNextNDays(noDaysToCheck)(config), 10.seconds)
          .flatten

        // alert if any of the crosswords are not ready
        alertForBadCrosswords(statuses)(config)

        s"checked $noDaysToCheck days"
      }
    } else if (event.containsKey("dateToCheck")) {
      val requestBuilder =
        new RequestBuilderWithSigner(
          config.capiPreviewRole,
          Constants.awsRegion.toString
        )

      val dateToCheck = event.get("dateToCheck").toString
      val d = LocalDate.parse(dateToCheck)

      val dateStatusFt =
        getAllCrosswordStatusesForDate(d)(config, requestBuilder)
      val dateStatus = Await.result(dateStatusFt, 10 seconds)

      val jsonStatus = write(dateStatus)(DefaultFormats)
      println(s"JSON status of crosswords for ${d.toString}: $jsonStatus")
      jsonStatus
    } else {
      "Crossword id and type or type and check query must be provided"
    }
  }

}
