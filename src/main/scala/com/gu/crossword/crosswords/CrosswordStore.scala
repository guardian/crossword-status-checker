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

  private def getMatchingCrosswordXMLKeys(id: String, crosswordType: String, bucketName: String)(config: Config): List[String] = {
    val xmlFiles = config.s3Client.listObjects(bucketName).getObjectSummaries.toList.map(_.getKey).filter(_.contains(".xml"))
    val exactMatch = xmlFiles.filter(_ == s"$id.xml")
    exactMatch.length match {
      case 1 => exactMatch
      case _ =>
        val withMatchingId = xmlFiles.filter(_.contains(id))
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
    val filesInForProcessing = getMatchingCrosswordXMLKeys(id, crosswordType, config.forProcessingBucketName)(config)
    val filesInProcessed = getMatchingCrosswordXMLKeys(id, crosswordType, config.processedBucketName)(config)
    models.CrosswordS3Status(getInBucketStatus(filesInForProcessing.length), filesInForProcessing,
      getInBucketStatus(filesInProcessed.length), filesInProcessed)
  }
}
