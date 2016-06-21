package com.gu.crossword

import java.util.Properties
import com.amazonaws.regions.{ Regions, Region }
import com.amazonaws.services.lambda.runtime.Context
import com.gu.crossword.services.S3.s3Client
import scala.util.Try

class Config(val context: Context) {

  val isProd = Try(context.getFunctionName.toLowerCase.contains("-prod")).getOrElse(true)
  private val stage = if (isProd) "PROD" else "CODE"
  private val config = loadConfig()

  def getConfig(property: String) = Option(config.getProperty(property)) getOrElse sys.error(s"'$property' property missing.")

  val s3BaseUrl = "https://console.aws.amazon.com/s3/home?region=eu-west-1#&bucket="
  val forProcessingBucketName: String = "crossword-files-for-processing"
  val processedBucketName: String = "crossword-processed-files"

  val crosswordMicroAppUrl = getConfig("crosswordmicroapp.url")
  val crosswordMicroAppKey = getConfig("crosswordmicroapp.key")

  val capiUrl = getConfig("capi.live.url")
  val capiKey = getConfig("capi.key")

  val capiPreviewUrl = getConfig("capi.preview.url")
  val capiPreviewUser = getConfig("capi.preview.user")
  val capiPreviewPassword = getConfig("capi.preview.password")
  val flexUrl = getConfig("flex.api.loadbalancer")
  val flexFindByPathEndpoint = getConfig("flex.api.findbypathendpoint")

  val composerApiUrl = getConfig("composer.url")
  val composerFindByPathEndpoint = getConfig("composer.findbypathendpoint")

  private def loadConfig() = {
    val configFileKey = s"$stage/config.properties"
    val configInputStream = s3Client.getObject("crossword-status-checker-config", configFileKey).getObjectContent
    val configFile: Properties = new Properties()
    Try(configFile.load(configInputStream)) orElse sys.error("Could not load config file from s3. This lambda will not run.")
    configFile
  }
}
