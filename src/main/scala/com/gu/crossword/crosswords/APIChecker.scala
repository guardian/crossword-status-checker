package com.gu.crossword.crosswords

import java.io.IOException

import com.gu.crossword.Config
import com.gu.crossword.crosswords.models.{APIStatus, CrosswordApiLocations}
import okhttp3._

import scala.concurrent.{Future, Promise}

object APIChecker {

  private val http = new OkHttpClient()

  private def getApiLocations(
      path: String
  )(config: Config): CrosswordApiLocations = {
    val flexUrl = s"${config.flexUrl}${config.flexFindByPathEndpoint}"
    CrosswordApiLocations(
      microappUrl =
        s"${config.crosswordMicroAppUrl}/api/$path.json?api-key=${config.crosswordMicroAppKey}&show-unpublished=true",
      flexDraftUrl = s"$flexUrl/$path/preview",
      flexLiveUrl = s"$flexUrl/$path/live",
      capiPreviewUrl = s"${config.capiPreviewUrl}/$path",
      capiLiveUrl = s"${config.capiUrl}/$path?api-key=${config.capiKey}"
    )
  }

  def checkIfCrosswordInApis(
      path: String
  )(config: Config, builder: RequestBuilder): Future[APIStatus] = {

    import scala.concurrent.ExecutionContext.Implicits.global

    def check200(req: Request): Future[Boolean] = {
      val promise = Promise[Boolean]()

      http
        .newCall(req)
        .enqueue(new Callback() {
          override def onFailure(call: Call, e: IOException): Unit =
            promise.failure(e)
          override def onResponse(call: Call, resp: Response): Unit = {
            if (resp.code == 200) {
              promise.success(true)
            } else {
              // we don't want to log live capi failures, as these are expected for all future crosswords and make the logging messy
              if (
                !req.url.host.contains("guardianapis.com") || req.url.host
                  .contains("preview")
              ) {
                println(
                  s"Didn't get 200 response from ${req.url}. Actual response: ${resp.message} ${resp.code}"
                )
                println(s"Response body: ${resp.body.string}")
              }
              promise.success(false)
            }
          }
        })

      promise.future
    }

    val apiLocations = getApiLocations(path)(config)

    val microappStatus = check200(
      builder.buildRequest(apiLocations.microappUrl, false)
    )

    val flexDraftStatus = check200(
      builder.buildRequest(apiLocations.flexDraftUrl, false)
    )
    val flexLiveStatus = check200(
      builder.buildRequest(apiLocations.flexLiveUrl, false)
    )
    val liveCapiStatus = check200(
      builder.buildRequest(apiLocations.capiLiveUrl, false)
    )
    val previewCapiStatus = check200(
      builder.buildRequest(apiLocations.capiPreviewUrl, true)
    )

    for {
      ms <- microappStatus
      fds <- flexDraftStatus
      fls <- flexLiveStatus
      pcs <- previewCapiStatus
      lcs <- liveCapiStatus
    } yield APIStatus(
      inCrosswordMicroApp = ms,
      inFlexDraftAPI = fds,
      inFlexLiveApi = fls,
      inCapiPreview = pcs,
      inLiveCapi = lcs
    )
  }

}
