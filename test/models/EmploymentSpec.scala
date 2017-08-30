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

package models

import models.taxhistory._
import org.joda.time.LocalDate
import play.api.data.validation.ValidationError
import play.api.libs.json.{JsError, JsPath, JsSuccess, Json}
import uk.gov.hmrc.play.test.UnitSpec


class EmploymentSpec extends UnitSpec {

  "Employment" should {
    "deserialize from json correctly" in {

      val json = Json.parse(
        """{
           "employments":[{"payeReference":"AA12341234",
                         "employerName":"Test Employer Name",
                         "taxablePayTotal":25000,
                         "taxTotal":2000,
                         "earlierYearUpdates" : [
                           {
                             "taxablePayEYU":1000,
                             "taxEYU":250,
                             "receivedDate":"2017-08-30"
                           }
                         ],
                         "companyBenefits":[{"typeDescription":"Benifit1","amount":1000},{"typeDescription":"Benifit2","amount":2000}]}],
           "allowances":[{"typeDescription":"desc","amount":222}]
        }""")


      val testEmployment = Employment("AA12341234", "Test Employer Name", Some(25000.0), Some(2000.0),
        List(EarlierYearUpdate(taxablePayEYU = BigDecimal.valueOf(1000), taxEYU = BigDecimal.valueOf(250),
          LocalDate.parse("2017-08-30"))),
        List(CompanyBenefit("Benifit1", 1000.00), CompanyBenefit("Benifit2", 2000.00)))
      val paye = PayAsYouEarnDetails(List(testEmployment), List(Allowance("desc", 222.00)))

      PayAsYouEarnDetails.formats.reads(json) shouldBe JsSuccess(paye)
    }

    "deserialize from json correctly" when {
      "earlierYearUpdates list is empty" in {

        val json = Json.parse(
          """{
           "employments":[{"payeReference":"AA12341234",
                         "employerName":"Test Employer Name",
                         "taxablePayTotal":25000,
                         "taxTotal":2000,
                         "earlierYearUpdates" : [],
                         "companyBenefits":[{"typeDescription":"Benifit1","amount":1000},{"typeDescription":"Benifit2","amount":2000}]}],
           "allowances":[{"typeDescription":"desc","amount":222}]
        }""")


        val testEmployment = Employment("AA12341234", "Test Employer Name", Some(25000.0), Some(2000.0),
          List.empty,
          List(CompanyBenefit("Benifit1", 1000.00), CompanyBenefit("Benifit2", 2000.00)))
        val paye = PayAsYouEarnDetails(List(testEmployment), List(Allowance("desc", 222.00)))

        PayAsYouEarnDetails.formats.reads(json) shouldBe JsSuccess(paye)
      }

      "earlierYearUpdates is missing in json" in {

        val json = Json.parse(
          """{
           "employments":[{"payeReference":"AA12341234",
                         "employerName":"Test Employer Name",
                         "taxablePayTotal":25000,
                         "taxTotal":2000,
                         "companyBenefits":[{"typeDescription":"Benifit1","amount":1000},{"typeDescription":"Benifit2","amount":2000}]}],
           "allowances":[{"typeDescription":"desc","amount":222}]
        }""")

        PayAsYouEarnDetails.formats.reads(json) shouldBe JsError((JsPath \ "employments" \0 \"earlierYearUpdates", ValidationError(List("error.path.missing"))))
      }
    }
  }

}
