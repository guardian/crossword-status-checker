package com.gu.crossword.services

import com.amazonaws.auth.AWSCredentialsProviderChain
import com.amazonaws.services.sns.{AmazonSNSClientBuilder}
import com.amazonaws.services.sns.model.PublishRequest
import com.gu.crossword.Config

object SNS {

  private def client = AmazonSNSClientBuilder
    .standard()
    .withRegion(Constants.awsRegion)
    .withCredentials(CredentialsProvider.awsCredentialsProvider)
    .build()

  def publishMessage(message: String)(config: Config) = {

    val publishRequest =
      new PublishRequest(config.alertTopic, message, "Crossword not ready")

    try {
      client.publish(publishRequest)
    } catch {
      case e: Exception =>
        println(s"SNS publishing failed with error: ${e.getMessage}")
    }

  }
}
