package com.gu.crossword.crosswords

import java.io.IOException
import java.net.URI

import com.gu.contentapi.client.IAMSigner
import com.gu.crossword.Config
import com.gu.crossword.crosswords.models.{ APIStatus, CrosswordApiLocations }
import okhttp3._

import scala.concurrent.{ Future, Promise }
import collection.JavaConverters._

trait APIChecker {

  private val http = new OkHttpClient()

  def getApiLocations(path: String)(config: Config): CrosswordApiLocations = {
    val flexUrl = s"${config.flexUrl}${config.flexFindByPathEndpoint}"
    CrosswordApiLocations(
      s"${config.crosswordMicroAppUrl}/api/$path.json?api-key=${config.crosswordMicroAppKey}&show-unpublished=true",
      s"$flexUrl/$path/preview",
      s"$flexUrl/$path/live",
      s"${config.capiPreviewUrl}/$path",
      s"${config.capiUrl}/$path?api-key=${config.capiKey}"
    )
  }

  def checkIfCrosswordInApis(path: String)(config: Config): Future[APIStatus] = {

    import scala.concurrent.ExecutionContext.Implicits.global

    def check200(req: Request): Future[Boolean] = {
      val promise = Promise[Boolean]()

      http.newCall(req).enqueue(new Callback() {
        override def onFailure(call: Call, e: IOException): Unit = promise.failure(e)
        override def onResponse(call: Call, resp: Response): Unit = {
          if (resp.code == 200) {
            promise.success(true)
          } else {
            // we don't want to log live capi failures, as these are expected for all future crosswords and make the logging messy
            if (!req.url.host.contains("guardianapis.com") || req.url.host.contains("preview")) {
              println(s"Didn't get 200 response from ${req.url}. Actual response: ${resp.message} ${resp.code}")
              println(s"Response body: ${resp.body.string}")
            }
            promise.success(false)
          }
        }
      })

      promise.future
    }

    def buildRequest(reqUrl: String) = new Request.Builder().url(reqUrl).build

    def buildReqWithAuth(reqUrl: String, signer: IAMSigner) = new Request.Builder().url(reqUrl).headers {
      val headers = signer.addIAMHeaders(headers = Map.empty, uri = new URI(reqUrl))
      Headers.of(headers.asJava)
    }.build

    val apiLocations = getApiLocations(path)(config)

    val microappStatus = check200(buildRequest(apiLocations.microappUrl))

    val flexDraftStatus = check200(buildRequest(apiLocations.flexDraftUrl))
    val flexLiveStatus = check200(buildRequest(apiLocations.flexLiveUrl))
    val liveCapiStatus = check200(buildRequest(apiLocations.capiLiveUrl))
    val previewCapiStatus = check200(buildReqWithAuth(apiLocations.capiPreviewUrl, config.signer))

    for {
      ms <- microappStatus
      fds <- flexDraftStatus
      fls <- flexLiveStatus
      pcs <- previewCapiStatus
      lcs <- liveCapiStatus
    } yield APIStatus(ms, fds, fls, pcs, lcs)
  }

}
