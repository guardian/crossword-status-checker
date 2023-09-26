package com.gu.crossword.crosswords

import com.gu.crossword.Config
import com.gu.crossword.crosswords.models.APIStatus
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import okhttp3.mockwebserver.{
  MockResponse,
  MockWebServer,
  RecordedRequest,
  Dispatcher
}

import scala.concurrent.duration._
import scala.concurrent.Await

class APICheckerTest extends AnyFlatSpec with Matchers {

  val emptyConfig = Config(
    forProcessingBucketName = "",
    processedBucketName = "",
    processedPdfBucketName = "",
    crosswordMicroAppUrl = "",
    crosswordMicroAppKey = "",
    capiUrl = "",
    capiKey = "",
    capiPreviewUrl = "",
    capiPreviewRole = "",
    flexUrl = "",
    flexFindByPathEndpoint = "",
    composerApiUrl = "",
    composerFindByPathEndpoint = "",
    alertTopic = ""
  )

  it should "return passing statuses if API calls return 200s" in {
    val requestBuilder = new BasicRequestBuilder()
    val mockHttpServer = new MockWebServer()
    mockHttpServer.start()
    val baseUrl = mockHttpServer.url("").toString
    val config = emptyConfig.copy(
      crosswordMicroAppUrl = baseUrl,
      capiUrl = baseUrl,
      capiPreviewUrl = baseUrl,
      flexUrl = baseUrl,
      flexFindByPathEndpoint = baseUrl,
      composerApiUrl = baseUrl,
      composerFindByPathEndpoint = baseUrl
    )

    mockHttpServer.enqueue(new MockResponse().setResponseCode(200))
    mockHttpServer.enqueue(new MockResponse().setResponseCode(200))
    mockHttpServer.enqueue(new MockResponse().setResponseCode(200))
    mockHttpServer.enqueue(new MockResponse().setResponseCode(200))
    mockHttpServer.enqueue(new MockResponse().setResponseCode(200))

    val statuses =
      APIChecker.checkIfCrosswordInApis("quick/1")(config, requestBuilder)
    Await.result(statuses, 1.seconds) mustBe APIStatus(
      true, true, true, true, true
    )

  }

  it should "return failing statuses if API calls return non-200s" in {
    val requestBuilder = new BasicRequestBuilder()
    val mockHttpServer = new MockWebServer()
    mockHttpServer.start()
    val baseUrl = mockHttpServer.url("").toString
    val config = emptyConfig.copy(
      crosswordMicroAppUrl = baseUrl,
      capiUrl = baseUrl,
      capiPreviewUrl = baseUrl,
      flexUrl = baseUrl,
      flexFindByPathEndpoint = baseUrl,
      composerApiUrl = baseUrl,
      composerFindByPathEndpoint = baseUrl
    )

    mockHttpServer.enqueue(new MockResponse().setResponseCode(400))
    mockHttpServer.enqueue(new MockResponse().setResponseCode(300))
    mockHttpServer.enqueue(new MockResponse().setResponseCode(404))
    mockHttpServer.enqueue(new MockResponse().setResponseCode(500))
    mockHttpServer.enqueue(new MockResponse().setResponseCode(501))

    val statuses =
      APIChecker.checkIfCrosswordInApis("quick/1")(config, requestBuilder)
    Await
      .result(statuses, 1.seconds) mustBe APIStatus(
      false, false, false, false, false
    )
  }

  it should "return mixed statuses depending on API responses" in {
    val requestBuilder = new BasicRequestBuilder()
    val mockHttpServer = new MockWebServer()
    mockHttpServer.start()
    val baseUrl = mockHttpServer.url("").toString
    val config = emptyConfig.copy(
      crosswordMicroAppUrl = baseUrl,
      crosswordMicroAppKey = "MICROAPP_REQUEST_IDENTIFIER",
      capiUrl = baseUrl,
      capiPreviewUrl = baseUrl,
      flexUrl = baseUrl,
      flexFindByPathEndpoint = baseUrl,
      composerApiUrl = baseUrl,
      composerFindByPathEndpoint = baseUrl
    )

    val dispatcher = new Dispatcher() {
      def dispatch(request: RecordedRequest): MockResponse = {
        request.getPath match {
          case path if path.contains("MICROAPP_REQUEST_IDENTIFIER") =>
            new MockResponse().setResponseCode(200)
          case _ =>
            new MockResponse().setResponseCode(300)
        }
      }
    }
    mockHttpServer.setDispatcher(dispatcher)

    val statuses =
      APIChecker.checkIfCrosswordInApis("quick/1")(config, requestBuilder)
    Await
      .result(statuses, 1.seconds) mustBe APIStatus(
      true, false, false, false, false
    )

  }

}
