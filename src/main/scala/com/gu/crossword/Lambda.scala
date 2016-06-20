package com.gu.crossword

import java.util.{ Map => JMap }

import com.amazonaws.services.lambda.runtime.{ Context, RequestHandler }
import com.gu.crossword.crosswords.models.CrosswordStatus
import com.gu.crossword.crosswords.{ APIChecker, CrosswordStore }

import scala.concurrent.duration._
import scala.concurrent.Await

class Lambda
    extends RequestHandler[JMap[String, Object], Unit]
    with APIChecker
    with CrosswordStore {

  override def handleRequest(event: JMap[String, Object], context: Context): Unit = {

    implicit val config = new Config(context)

    val crosswordType = "cryptic"
    val crosswordId = "26915"

    println("Getting crossword status.")

    val s3Status = checkCrosswordS3Status(crosswordId, crosswordType)
    val path = s"crosswords/$crosswordType/$crosswordId"
    val apiStatus = checkIfCrosswordInApis(path)(config)

    import scala.language.postfixOps
    val status = CrosswordStatus(s3Status, Await.result(apiStatus, 10 seconds))

    println(status)

  }

}