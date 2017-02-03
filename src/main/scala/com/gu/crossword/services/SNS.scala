package com.gu.crossword.services

import com.amazonaws.auth.AWSCredentialsProviderChain
import com.amazonaws.services.sns.{ AmazonSNSClientBuilder }
import com.amazonaws.services.sns.model.PublishRequest
import com.gu.crossword.Config

object SNS {
  def getSNSClient(credentialsProviderChain: AWSCredentialsProviderChain, region: String) = {
    val client = AmazonSNSClientBuilder.standard().withRegion(region).withCredentials(credentialsProviderChain).build()
    client
  }

  def publishMessage(message: String)(config: Config) = {

    val publishRequest = new PublishRequest(config.alertTopic, message, "Crossword not ready")

    try {
      config.snsClient.publish(publishRequest)
    } catch {
      case e: Exception => println(s"SNS publishing failed with error: ${e.getMessage}")
    }

  }
}
