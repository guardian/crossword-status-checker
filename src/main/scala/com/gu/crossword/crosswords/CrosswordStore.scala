package com.gu.crossword.crosswords

import com.gu.crossword.Config
import com.gu.crossword.services.S3.getS3Client

import org.joda.time.{LocalDate}
import scala.jdk.CollectionConverters._

trait CrosswordStore {

  def filterByType(files: List[String], crosswordType: String): Option[String] = {
    val filteredByType = files.filter(_.toUpperCase.contains(crosswordType.take(4).toUpperCase))
    if (filteredByType.length == 1) filteredByType.headOption
    else None
  }

  private def getMatchingCrosswordFileKeys(id: Option[String], crosswordType: String, bucketName: String, format: String)(config: Config): List[String] = {
    if (id.isDefined) {
      val files = config.s3Client.listObjects(bucketName, s"${id.get}.$format")
        .getObjectSummaries
        .asScala
        .toList
        .map(_.getKey)
      val exactMatch = files.filter(_ == s"${id.get}.$format")
      exactMatch.length match {
        case 1 => exactMatch
        case _ =>
          val withMatchingId = files.filter(_.contains(id))
          if (withMatchingId.isEmpty || withMatchingId.length == 1) withMatchingId
          else filterByType(withMatchingId, crosswordType).fold(withMatchingId)(List(_))
      }
    } else List.empty
  }

  private def getInBucketStatus(numMatchingKeys: Int) = {
    numMatchingKeys match {
      case 0 => "no"
      case 1 => "yes"
      case _ => "maybe"
    }
  }

  def getPdfIdForCrosswordNo(id: String, crosswordType: String): Option[String] = {
    val xWordHelper = models.CrosswordTypeHelpers.getXWordType(crosswordType)
    val xWordDate = xWordHelper.getDate(id.toInt)
    xWordDate.map(date => s"$crosswordType.${date.toString("yyyyMMdd")}")
  }

  def checkCrosswordS3Status(id: String, crosswordType: String)(implicit config: Config) = {
    val filesInForProcessing = getMatchingCrosswordFileKeys(Some(id), crosswordType, config.forProcessingBucketName, "xml")(config)
    val filesInProcessed = getMatchingCrosswordFileKeys(Some(id), crosswordType, config.processedBucketName, "xml")(config)
    val pdfsInForProcessing = getMatchingCrosswordFileKeys(getPdfIdForCrosswordNo(id, crosswordType), crosswordType, config.forProcessingBucketName, "pdf")(config)
    val pdfsInProcessed = getMatchingCrosswordFileKeys(getPdfIdForCrosswordNo(id, crosswordType), crosswordType, config.processedPdfBucketName, "pdf")(config)

    models.CrosswordS3Status(getInBucketStatus(filesInForProcessing.length), filesInForProcessing,
      getInBucketStatus(filesInProcessed.length), filesInProcessed, getInBucketStatus(pdfsInForProcessing.length), pdfsInForProcessing, getInBucketStatus(pdfsInProcessed.length), pdfsInProcessed)
  }
}
