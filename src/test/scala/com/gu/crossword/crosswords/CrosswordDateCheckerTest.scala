package com.gu.crossword.crosswords

import com.gu.crossword.Config
import com.gu.crossword.crosswords.models.CrosswordReadyStatus
import okhttp3.mockwebserver.{
  Dispatcher,
  MockResponse,
  MockWebServer,
  RecordedRequest
}
import org.joda.time.LocalDate
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class CrosswordDateCheckerTest extends AnyFlatSpec with Matchers {

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

  it should "return passing statuses if CAPI Preview calls return 200s" in {
    val requestBuilder = new BasicRequestBuilder()
    val mockHttpServer = new MockWebServer()
    mockHttpServer.start()
    val baseUrl = mockHttpServer.url("").toString

    val capiPreviewIdentifier = "CAPI_PREVIEW_REQUEST_IDENTIFIER"

    val config = emptyConfig.copy(
      crosswordMicroAppUrl = baseUrl,
      capiUrl = baseUrl,
      capiPreviewUrl = s"$baseUrl/$capiPreviewIdentifier",
      flexUrl = baseUrl,
      flexFindByPathEndpoint = baseUrl,
      composerApiUrl = baseUrl,
      composerFindByPathEndpoint = baseUrl
    )

    val dispatcher = new Dispatcher() {
      def dispatch(request: RecordedRequest): MockResponse = {
        request.getPath match {
          case path if path.contains(capiPreviewIdentifier) =>
            new MockResponse().setResponseCode(200)
          case _ =>
            new MockResponse().setResponseCode(300)
        }
      }
    }
    mockHttpServer.setDispatcher(dispatcher)

    val statuses = CrosswordDateChecker.getAllCrosswordStatusesForDate(
      new LocalDate(2023, 9, 27)
    )(config, requestBuilder)

    Await.result(statuses, 10.seconds) mustBe List(
      CrosswordReadyStatus("quick", 16659, true, new LocalDate("2023-09-27")),
      CrosswordReadyStatus("cryptic", 29186, true, new LocalDate("2023-09-27"))
    )

  }

  it should "return failing statuses for any CAPI Preview calls that return not-200s" in {
    val requestBuilder = new BasicRequestBuilder()
    val mockHttpServer = new MockWebServer()
    mockHttpServer.start()
    val baseUrl = mockHttpServer.url("").toString

    val capiPreviewIdentifier = "CAPI_PREVIEW_REQUEST_IDENTIFIER"

    val config = emptyConfig.copy(
      crosswordMicroAppUrl = baseUrl,
      capiUrl = baseUrl,
      capiPreviewUrl = s"$baseUrl/$capiPreviewIdentifier",
      flexUrl = baseUrl,
      flexFindByPathEndpoint = baseUrl,
      composerApiUrl = baseUrl,
      composerFindByPathEndpoint = baseUrl
    )

    val dispatcher = new Dispatcher() {
      def dispatch(request: RecordedRequest): MockResponse = {
        request.getPath match {
          case path
              if path
                .contains(capiPreviewIdentifier) && path.contains("quick") =>
            new MockResponse().setResponseCode(200)
          case _ =>
            new MockResponse().setResponseCode(404)
        }
      }
    }
    mockHttpServer.setDispatcher(dispatcher)

    val statuses = CrosswordDateChecker.getAllCrosswordStatusesForDate(
      new LocalDate(2023, 9, 27)
    )(config, requestBuilder)

    Await.result(statuses, 10.seconds) mustBe List(
      CrosswordReadyStatus("quick", 16659, true, new LocalDate("2023-09-27")),
      CrosswordReadyStatus("cryptic", 29186, false, new LocalDate("2023-09-27"))
    )

  }



}
