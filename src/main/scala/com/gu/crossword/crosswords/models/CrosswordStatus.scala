package com.gu.crossword.crosswords.models

import org.joda.time.LocalDate

import org.json4s._
import org.json4s.native.Serialization
import org.json4s.native.Serialization.write

case class CrosswordS3Status(
    inForProcessingBucket: String,
    matchingKeysInForProcessingBucket: List[String],
    inProcessedBucket: String,
    matchingKeysInProcessedBucket: List[String],
    pdfInForProcessingBucket: String,
    matchingPdfKeysInProcessingBucket: List[String],
    inProcessedPdfBucket: String,
    matchingKeysInProcessedPdfBucket: List[String]
)
case class APIStatus(
    inCrosswordMicroApp: Boolean,
    inFlexDraftAPI: Boolean,
    inFlexLiveApi: Boolean,
    inCapiPreview: Boolean,
    inLiveCapi: Boolean = false,
    inCrosswordMicroAppV2: Boolean
)
case class CrosswordApiLocations(
    microappUrl: String,
    flexDraftUrl: String,
    flexLiveUrl: String,
    capiPreviewUrl: String,
    capiLiveUrl: String,
    microappV2Url: String
)
case class CrosswordStatus(s3Status: CrosswordS3Status, apiStatus: APIStatus)
case class CrosswordReadyStatus(
    crosswordType: String,
    number: Int,
    ready: Boolean,
    date: LocalDate
)

object APIStatus {
  def toJson(status: APIStatus) = {
    implicit val formats = Serialization.formats(NoTypeHints)
    val json = write(status)
    json
  }
}

object CrosswordStatus {

  def toJson(status: CrosswordStatus) = {
    implicit val formats = Serialization.formats(NoTypeHints)
    val json = write(status)
    json
  }
}
