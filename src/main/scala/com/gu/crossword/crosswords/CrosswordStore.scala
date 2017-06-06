package com.gu.crossword.crosswords

import com.gu.crossword.Config
import com.gu.crossword.services.S3.getS3Client

import scala.collection.JavaConversions._

trait CrosswordStore {

  def filterByType(files: List[String], crosswordType: String): Option[String] = {
    val filteredByType = files.filter(_.toUpperCase.contains(crosswordType.take(4).toUpperCase))
    if (filteredByType.length == 1) filteredByType.headOption
    else None
  }

  private def getMatchingCrosswordFileKeys(id: String, crosswordType: String, bucketName: String, format: String)(config: Config): List[String] = {
    val files = config.s3Client.listObjects(bucketName).getObjectSummaries.toList.map(_.getKey).filter(_.contains(s".$format"))
    val exactMatch = files.filter(_ == s"$id.$format")
    exactMatch.length match {
      case 1 => exactMatch
      case _ =>
        val withMatchingId = files.filter(_.contains(id))
        if (withMatchingId.isEmpty || withMatchingId.length == 1) withMatchingId
        else filterByType(withMatchingId, crosswordType).fold(withMatchingId)(List(_))
    }
  }

  private def getInBucketStatus(numMatchingKeys: Int) = {
    numMatchingKeys match {
      case 0 => "no"
      case 1 => "yes"
      case _ => "maybe"
    }
  }

  def checkCrosswordS3Status(id: String, crosswordType: String)(implicit config: Config) = {
    val filesInForProcessing = getMatchingCrosswordFileKeys(id, crosswordType, config.forProcessingBucketName, "xml")(config)
    val filesInProcessed = getMatchingCrosswordFileKeys(id, crosswordType, config.processedBucketName, "xml")(config)
    val pdfsInForProcessing = getMatchingCrosswordFileKeys(id, crosswordType, config.forProcessingBucketName, "pdf")(config)
    val pdfsInProcessed = getMatchingCrosswordFileKeys(id, crosswordType, config.processedPdfBucketName, "pdf")(config)
    models.CrosswordS3Status(getInBucketStatus(filesInForProcessing.length), filesInForProcessing,
      getInBucketStatus(filesInProcessed.length), filesInProcessed, getInBucketStatus(pdfsInForProcessing.length), pdfsInForProcessing, getInBucketStatus(pdfsInProcessed.length), pdfsInProcessed)
  }
}
