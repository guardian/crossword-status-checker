package com.gu.crossword.crosswords

import com.gu.crossword.Config
import com.gu.crossword.crosswords.models.APIStatus
import dispatch._

import scala.concurrent.Future

trait APIChecker {

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

    val microappStatus = check200(buildRequest(s"${config.crosswordMicroAppUrl}/api/$path.json?api-key=${config.crosswordMicroAppKey}"))

    val flexDraftStatus = check200(buildRequest(s"${config.flexUrl}${config.flexFindByPathEndpoint}/$path/preview"))
    val flexLiveStatus = check200(buildRequest(s"${config.flexUrl}${config.flexFindByPathEndpoint}/$path/live"))
    val liveCapiStatus = check200(buildRequest(s"${config.capiUrl}/$path?api-key=${config.capiKey}"))
    val previewCapiStatus = check200(buildReqBasicAuth(s"${config.capiPreviewUrl}/$path", config.capiPreviewUser, config.capiPreviewPassword))

    for {
      ms <- microappStatus
      fds <- flexDraftStatus
      fls <- flexLiveStatus
      pcs <- previewCapiStatus
      lcs <- liveCapiStatus
    } yield APIStatus(ms, fds, fls, pcs, lcs)
  }

}
