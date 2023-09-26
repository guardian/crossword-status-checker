package com.gu.crossword

import java.util.Properties
import com.amazonaws.services.lambda.runtime.Context
import com.gu.crossword.crosswords.{RequestBuilder, RequestBuilderWithSigner}
import com.gu.crossword.services.{Constants, S3}

import scala.util.Try

case class Config(
    forProcessingBucketName: String,
    processedBucketName: String,
    processedPdfBucketName: String,
    crosswordMicroAppUrl: String,
    crosswordMicroAppKey: String,
    capiUrl: String,
    capiKey: String,
    capiPreviewUrl: String,
    capiPreviewRole: String,
    flexUrl: String,
    flexFindByPathEndpoint: String,
    composerApiUrl: String,
    composerFindByPathEndpoint: String,
    alertTopic: String,
)

object Config {

  def fromContext(context: Context): Config = {
    val isProd =
      Try(context.getFunctionName.toLowerCase.contains("-prod")).getOrElse(true)
    val stage = if (isProd) "PROD" else "CODE"
    val config = loadConfig(stage)

    def getConfig(property: String) = Option(
      config.getProperty(property)
    ) getOrElse sys.error(s"'$property' property missing.")

    val forProcessingBucketName: String = "crossword-files-for-processing"
    val processedBucketName: String = "crossword-processed-files"

    val processedPdfBucketName: String = "crosswords-pdf-public-prod"

    val crosswordMicroAppUrl = getConfig("crosswordmicroapp.url")
    val crosswordMicroAppKey = getConfig("crosswordmicroapp.key")

    val capiUrl = getConfig("capi.live.url")
    val capiKey = getConfig("capi.key")

    val capiPreviewUrl = getConfig("capi.preview.iam-url")
    val capiPreviewRole = getConfig("capi.preview.role")

    val flexUrl = getConfig("flex.api.loadbalancer")
    val flexFindByPathEndpoint = getConfig("flex.api.findbypathendpoint")

    val composerApiUrl = getConfig("composer.url")
    val composerFindByPathEndpoint = getConfig("composer.findbypathendpoint")

    val alertTopic = getConfig("sns.alert.topic")

    Config(
      forProcessingBucketName = forProcessingBucketName,
      processedBucketName = processedBucketName,
      processedPdfBucketName = processedPdfBucketName,
      crosswordMicroAppUrl = crosswordMicroAppUrl,
      crosswordMicroAppKey = crosswordMicroAppKey,
      capiUrl = capiUrl,
      capiKey = capiKey,
      capiPreviewUrl = capiPreviewUrl,
      capiPreviewRole = capiPreviewRole,
      flexUrl = flexUrl,
      flexFindByPathEndpoint = flexFindByPathEndpoint,
      composerApiUrl = composerApiUrl,
      composerFindByPathEndpoint = composerFindByPathEndpoint,
      alertTopic = alertTopic,
    )
  }

  private def loadConfig(stage: String) = {
    val configFileKey = s"$stage/config.properties"
    val configInputStream =
      S3.client.getObject("crossword-status-checker-config", configFileKey)
    val context2 = configInputStream.getObjectContent
    val configFile: Properties = new Properties()
    Try(configFile.load(context2)) orElse sys.error(
      "Could not load config file from s3. This lambda will not run."
    )
    configFile
  }

}
