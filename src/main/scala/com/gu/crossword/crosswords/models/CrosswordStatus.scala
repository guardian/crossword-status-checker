package com.gu.crossword.crosswords.models

case class CrosswordS3Status(inForProcessingBucket: String, forProcessingKeys: List[String], inProcessedBucket: String, processedName: List[String])
case class APIStatus(microappStatus: Boolean, flexDraftStatus: Boolean, flexLiveStatus: Boolean, capiPreviewStatus: Boolean, capiLiveStatus: Boolean = false)

case class CrosswordStatus(s3Status: CrosswordS3Status, apiStatus: APIStatus)