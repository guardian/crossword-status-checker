package com.gu.crossword.crosswords

import com.gu.crossword.Config
import com.gu.crossword.crosswords.models.{ APIStatus, CrosswordApiLocations }
import dispatch._

import scala.concurrent.Future

trait APIChecker {

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

    val h = new Http()

    def check200(request: Req) = h(request).map(resp => {
      if (resp.getStatusCode == 200) {
        true
      } else {
        println(s"Didn't get 200 response from ${request.url}. Actual response: ${resp.getStatusText} ${resp.getStatusCode}")
        println(s"Response body: ${resp.getResponseBody}")
        false
      }
    })

    def buildRequest(reqUrl: String) = url(reqUrl)
    def buildReqBasicAuth(reqUrl: String, user: String, password: String) = url(reqUrl).as_!(user, password)

    val apiLocations = getApiLocations(path)(config)

    val microappStatus = check200(buildRequest(apiLocations.microappUrl))

    val flexDraftStatus = check200(buildRequest(apiLocations.flexDraftUrl))
    val flexLiveStatus = check200(buildRequest(apiLocations.flexLiveUrl))
    val liveCapiStatus = check200(buildRequest(apiLocations.capiLiveUrl))
    val previewCapiStatus = check200(buildReqBasicAuth(apiLocations.capiPreviewUrl, config.capiPreviewUser, config.capiPreviewPassword))

    for {
      ms <- microappStatus
      fds <- flexDraftStatus
      fls <- flexLiveStatus
      pcs <- previewCapiStatus
      lcs <- liveCapiStatus
    } yield APIStatus(ms, fds, fls, pcs, lcs)
  }

}
