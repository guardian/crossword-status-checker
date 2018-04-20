package com.gu.crossword

import java.util.Properties

import com.amazonaws.auth.{ AWSCredentialsProvider, AWSCredentialsProviderChain, DefaultAWSCredentialsProviderChain, STSAssumeRoleSessionCredentialsProvider }
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.{ Region, Regions }
import com.amazonaws.services.lambda.runtime.Context
import com.gu.contentapi.client.IAMSigner
import com.gu.crossword.services.S3.getS3Client
import com.gu.crossword.services.SNS.getSNSClient

import scala.util.Try

class Config(val context: Context) {

  val awsCredentialsProvider = new AWSCredentialsProviderChain(
    new ProfileCredentialsProvider("composer"),
    new DefaultAWSCredentialsProviderChain
  )

  val s3Client = getS3Client(awsCredentialsProvider)

  val isProd = Try(context.getFunctionName.toLowerCase.contains("-prod")).getOrElse(true)
  private val stage = if (isProd) "PROD" else "CODE"
  private val config = loadConfig()

  def getConfig(property: String) = Option(config.getProperty(property)) getOrElse sys.error(s"'$property' property missing.")

  val forProcessingBucketName: String = "crossword-files-for-processing"
  val processedBucketName: String = "crossword-processed-files"

  val processedPdfBucketName: String = "crosswords-pdf-public-prod"

  val awsRegion: String = "eu-west-1"

  val crosswordMicroAppUrl = getConfig("crosswordmicroapp.url")
  val crosswordMicroAppKey = getConfig("crosswordmicroapp.key")

  val capiUrl = getConfig("capi.live.url")
  val capiKey = getConfig("capi.key")

  val capiPreviewUrl = getConfig("capi.preview.iam-url")
  val capiPreviewRole = getConfig("capi.preview.role")
  val capiPreviewCredentials: AWSCredentialsProvider = {
    new AWSCredentialsProviderChain(
      new ProfileCredentialsProvider("capi"),
      new STSAssumeRoleSessionCredentialsProvider.Builder(capiPreviewRole, "capi").build()
    )
  }
  val signer = new IAMSigner(capiPreviewCredentials, awsRegion)

  val flexUrl = getConfig("flex.api.loadbalancer")
  val flexFindByPathEndpoint = getConfig("flex.api.findbypathendpoint")

  val composerApiUrl = getConfig("composer.url")
  val composerFindByPathEndpoint = getConfig("composer.findbypathendpoint")

  val snsClient = getSNSClient(awsCredentialsProvider, awsRegion)

  val alertTopic = getConfig("sns.alert.topic")

  private def loadConfig() = {
    val configFileKey = s"$stage/config.properties"
    val configInputStream = s3Client.getObject("crossword-status-checker-config", configFileKey)
    val context2 = configInputStream.getObjectContent
    val configFile: Properties = new Properties()
    Try(configFile.load(context2)) orElse sys.error("Could not load config file from s3. This lambda will not run.")
    configFile
  }
}
