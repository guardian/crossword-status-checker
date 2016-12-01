package com.gu.crossword.services

import com.amazonaws.auth.AWSCredentialsProviderChain
import com.amazonaws.regions.{ Region, Regions }
import com.amazonaws.services.sns.AmazonSNSAsyncClient
import com.amazonaws.services.sns.model.PublishRequest
import com.gu.crossword.Config

import scala.concurrent.{ Future, blocking }
import scala.util.Try

object SNS {
  def getSNSClient(credentialsProviderChain: AWSCredentialsProviderChain): AmazonSNSAsyncClient = {
    val client = new AmazonSNSAsyncClient(credentialsProviderChain)
    client.setRegion(Region.getRegion(Regions.EU_WEST_1))
    client
  }

  def publishMessage(message: String)(config: Config) = {
    import scala.concurrent.ExecutionContext.Implicits.global

    val publishRequest = new PublishRequest(config.flexLowUrgencySNSTopic, message)
    val res = Try(config.snsClient.publishAsync(publishRequest))
    res.map(pr => {
      Future {
        blocking {
          println("Publish result: " + pr.get)
          println("Message id: " + pr.get.getMessageId)
        }
      }
    })

  }
}
