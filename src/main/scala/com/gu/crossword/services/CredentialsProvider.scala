package com.gu.crossword.services

import com.amazonaws.auth.{
  AWSCredentialsProviderChain,
  DefaultAWSCredentialsProviderChain
}
import com.amazonaws.auth.profile.ProfileCredentialsProvider

object CredentialsProvider {
  val awsCredentialsProvider = new AWSCredentialsProviderChain(
    new ProfileCredentialsProvider(Constants.profile),
    new DefaultAWSCredentialsProviderChain
  )
}
