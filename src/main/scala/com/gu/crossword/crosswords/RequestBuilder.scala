package com.gu.crossword.crosswords

import com.gu.contentapi.client.IAMSigner
import okhttp3.{Headers, Request}

import java.net.URI
import scala.jdk.CollectionConverters._

trait RequestBuilder {
  val builder: Request.Builder

  def buildRequest(url: String, withAuth: Boolean): Request
}

class RequestBuilderWithSigner(signer: IAMSigner) extends RequestBuilder {
  val builder = new Request.Builder()

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
  val builder = new Request.Builder()

  def buildRequest(url: String, withAuth: Boolean) = builder.url(url).build
}
