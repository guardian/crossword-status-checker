package com.gu.crossword.services

import com.amazonaws.auth.AWSCredentialsProviderChain
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}

object S3 {
  def client: AmazonS3 =
    AmazonS3ClientBuilder
      .standard()
      .withCredentials(CredentialsProvider.awsCredentialsProvider)
      .build()

}
