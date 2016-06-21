package com.gu.crossword.crosswords.models

import org.json4s._
import org.json4s.native.Serialization
import org.json4s.native.Serialization.write

case class CrosswordS3Status(
  inForProcessingBucket: String,
  matchingKeysInForProcessingBucket: List[String],
  inProcessedBucket: String,
  matchingKeysInProcessedBucket: List[String])
case class APIStatus(inCrosswordMicroApp: Boolean, inFlexDraftAPI: Boolean, inFlexLiveApi: Boolean, inCapiPreview: Boolean, inLiveCapi: Boolean = false)
case class CrosswordApiLocations(microappUrl: String, flexDraftUrl: String, flexLiveUrl: String, capiPreviewUrl: String, capiLiveUrl: String)
case class CrosswordStatus(s3Status: CrosswordS3Status, apiStatus: APIStatus, crosswordApiLocations: CrosswordApiLocations)

object CrosswordStatus {

  def toJson(status: CrosswordStatus) = {
    implicit val formats = Serialization.formats(NoTypeHints)
    val json = write(status)
    json
  }
}