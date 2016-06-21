package com.gu.crossword

import java.util.{ Map => JMap }

import com.amazonaws.services.lambda.runtime.{ Context, RequestHandler }
import com.gu.crossword.crosswords.models.CrosswordStatus
import com.gu.crossword.crosswords.{ APIChecker, CrosswordStore }

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
      val status = CrosswordStatus(s3Status, Await.result(apiStatus, 10 seconds), getApiLocations(path, true)(config))

      val statusJson = CrosswordStatus.toJson(status)
      println(statusJson)
      statusJson
    } else {
      "Crossword id and type must be provided"
    }

  }

}