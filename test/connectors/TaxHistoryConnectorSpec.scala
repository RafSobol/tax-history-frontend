/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connectors

import java.util.UUID

import config.{ConfigDecorator, FrontendAuthConnector, WSHttpT}
import model.api.Allowance
import org.joda.time.LocalDate
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.Application
import play.api.http.Status
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import support.{BaseSpec, Fixtures}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HttpResponse
import models.taxhistory.Employment
import utils.TestUtil

import scala.concurrent.Future

class TaxHistoryConnectorSpec extends BaseSpec with MockitoSugar with Fixtures with TestUtil{

  val http = mock[WSHttpT]
  val startDate = new LocalDate("2016-01-21")

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[FrontendAuthConnector].toInstance(mock[FrontendAuthConnector]))
    .overrides(bind[ConfigDecorator].toInstance(mock[ConfigDecorator]))
    .overrides(bind[WSHttpT].toInstance(http))
    .build()

  trait LocalSetup {
    lazy val nino =randomNino.toString()
    lazy val connector = {
      injected[TaxHistoryConnector]
    }
  }

  "TaxHistoryConnector" should {

    "fetch tax history" in new LocalSetup {
      when(connector.httpGet.GET[HttpResponse](any())(any(), any(), any())).thenReturn(
        Future.successful(HttpResponse(Status.OK,Some(Json.toJson(Seq(Employment("12341234", "Test Employer Name",
          startDate, None, Some(25000.0), Some(2000.0))))))))

      val result = await(connector.getTaxHistory(Nino(nino), 2017))

      result.status shouldBe Status.OK
      result.json shouldBe Json.toJson(Seq(Employment("12341234", "Test Employer Name", startDate, None, Some(25000.0), Some(2000.0))))

    }

    "fetch allowance for tax history" in new LocalSetup {

      val allowance = Allowance(allowanceId = UUID.fromString("c9923a63-4208-4e03-926d-7c7c88adc7ee"),
        iabdType = "allowanceType",
        amount = BigDecimal(12.00))

      when(connector.httpGet.GET[HttpResponse](any())(any(), any(), any())).thenReturn(
        Future.successful(HttpResponse(Status.OK,Some(Json.toJson(Seq(allowance))))))

      val result = await(connector.getAllowances(Nino(nino), 2017))

      result.status shouldBe Status.OK
      result.json shouldBe Json.toJson(Seq(allowance))

    }
  }

}
