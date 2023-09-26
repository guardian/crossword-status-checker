package com.gu.crossword.crosswords

import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth.{AWSCredentialsProvider, AWSCredentialsProviderChain, STSAssumeRoleSessionCredentialsProvider}
import com.gu.contentapi.client.IAMSigner
import okhttp3.{Headers, Request}

import java.net.URI
import scala.jdk.CollectionConverters._

trait RequestBuilder {
  def buildRequest(url: String, withAuth: Boolean): Request
}

class RequestBuilderWithSigner(capiPreviewRole: String, awsRegion: String) extends RequestBuilder {
  private val builder = new Request.Builder()

  private val signer = {
    val capiPreviewCredentials: AWSCredentialsProvider = {
      new AWSCredentialsProviderChain(
        new ProfileCredentialsProvider("capi"),
        new STSAssumeRoleSessionCredentialsProvider.Builder(
          capiPreviewRole,
          "capi"
        ).build()
      )
    }

    new IAMSigner(capiPreviewCredentials, awsRegion)
  }

  def buildRequest(url: String, withAuth: Boolean) = {
    val req = builder
      .url(url)

    if (withAuth) {
      val headers =
        signer.addIAMHeaders(headers = Map.empty, uri = new URI(url))
      req.headers {
        Headers.of(headers.asJava)
      }
    }
    req.build
  }
}

class BasicRequestBuilder extends RequestBuilder {
  private val builder = new Request.Builder()

  def buildRequest(url: String, withAuth: Boolean) = builder.url(url).build
}
