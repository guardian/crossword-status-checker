package com.gu.crossword.services

import com.amazonaws.auth.AWSCredentialsProviderChain
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}

object S3 {
  def getS3Client(credentialsProviderChain: AWSCredentialsProviderChain): AmazonS3 =
    AmazonS3ClientBuilder.standard().withCredentials(credentialsProviderChain).build()

}
