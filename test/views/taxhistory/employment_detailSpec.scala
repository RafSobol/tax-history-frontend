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

package views.taxhistory

import java.util.UUID

import model.api.{CompanyBenefit, EarlierYearUpdate, PayAndTax}
import models.taxhistory._
import org.joda.time.LocalDate
import org.scalatest.MustMatchers
import play.api.i18n.Messages
import utils.TestUtil
import views.{Fixture, GenericTestHelper}

class employment_detailSpec extends GenericTestHelper with MustMatchers with DetailConstants {

  trait ViewFixture extends Fixture {
    implicit val requestWithToken = addToken(request)

    val nino = TestUtil.randomNino.toString()
    val taxYear = 2017
    val person = Some(Person(Some("James"),Some("Dean"),false))
  }

  "employment_detail view" must {

    "have correct title and heading should only show one h1" in new ViewFixture {

      val view = views.html.taxhistory.employment_detail(taxYear, payAndTax, List.empty)

      val title = Messages("employmenthistory.title")
      doc.title mustBe title

    }

    "have correct employment details" in new ViewFixture {

      val view = views.html.taxhistory.employment_detail(taxYear, payAndTax, List.empty)

      val taxablePay = doc.select("#employment-table tbody tr").get(0)
      val incomeTax = doc.select("#employment-table tbody tr").get(1)

      taxablePay.text must include("£4,896.80")
      incomeTax.text must include("£979.36")

    }

    "have correct Earlier Year Update details" in new ViewFixture {

      val view = views.html.taxhistory.employment_detail(taxYear, payAndTax, List.empty)
      
      val eyuRow0 = doc.select("#eyu-table tbody tr").get(0)
      val eyuRow1 = doc.select("#eyu-table tbody tr").get(1)

      eyuRow0.text must include("21 January 2016")
      eyuRow0.text must include("£0.00")
      eyuRow0.text must include("£8.99")

      eyuRow1.text must include("21 May 2016")
      eyuRow1.text must include("£10.00")
      eyuRow1.text must include("£18.99")

    }

     "have correct company benefits details" in  new ViewFixture  {

       val view = views.html.taxhistory.employment_detail(taxYear, payAndTax, completeCBList)

       completeCBList.foreach(cb => {
         doc.getElementsContainingOwnText(Messages(s"employmenthistory.cb.${cb.iabdType}")).hasText mustBe true
       })
     }
  }

}

trait DetailConstants {

  val eyu1 = EarlierYearUpdate(
    taxablePayEYU = 0,
    taxEYU = 8.99,
    receivedDate = LocalDate.parse("2016-01-21")
  )

  val eyu2 = EarlierYearUpdate(
    taxablePayEYU = 10,
    taxEYU = 18.99,
    receivedDate = LocalDate.parse("2016-05-21")
  )

  val eyuList = List(eyu1, eyu2)

  val payAndTax = PayAndTax(
    taxablePayTotal = Some(4896.80),
    taxTotal = Some(979.36),
    earlierYearUpdates = eyuList
  )
  val uuid = UUID.randomUUID()

  val completeCBList = List(CompanyBenefit(uuid, "EmployerProvidedServices", 1000.00),
    CompanyBenefit(uuid, "CarFuelBenefit", 1000),
    CompanyBenefit(uuid, "MedicalInsurance", 1000.00),
    CompanyBenefit(uuid, "CarBenefit", 1000.00),
    CompanyBenefit(uuid, "TelephoneBenefit", 1000.00),
    CompanyBenefit(uuid, "ServiceBenefit", 1000.00),
    CompanyBenefit(uuid, "TaxableExpenseBenefit", 1000.00),
    CompanyBenefit(uuid, "VanBenefit", 1000.00),
    CompanyBenefit(uuid, "VanFuelBenefit", 1000.00),
    CompanyBenefit(uuid, "BeneficialLoan", 1000.00),
    CompanyBenefit(uuid, "TotalBenefitInKind", 1000.00),
    CompanyBenefit(uuid, "Accommodation", 1000.00),
    CompanyBenefit(uuid, "Assets", 1000.00),
    CompanyBenefit(uuid, "AssetTransfer", 1000.00),
    CompanyBenefit(uuid, "EducationalService", 1000.00),
    CompanyBenefit(uuid, "Entertaining", 1000.00),
    CompanyBenefit(uuid, "ExpensesPay", 1000.00),
    CompanyBenefit(uuid, "Mileage", 1000.00),
    CompanyBenefit(uuid, "NonQualifyingRelocationExpense", 1000.00),
    CompanyBenefit(uuid, "NurseryPlaces", 1000.00),
    CompanyBenefit(uuid, "OtherItems", 1000.00),
    CompanyBenefit(uuid, "PaymentEmployeesBehalf", 1000.00),
    CompanyBenefit(uuid, "PersonalIncidentExpenses", 1000.00),
    CompanyBenefit(uuid, "QualifyingRelocationExpenses", 1000.00),
    CompanyBenefit(uuid, "EmployerProvidedProfessionalSubscription", 1000.00),
    CompanyBenefit(uuid, "IncomeTaxPaidNotDeductedFromDirectorsRemuneration", 1000.00),
    CompanyBenefit(uuid, "TravelAndSubsistence", 1000.00),
    CompanyBenefit(uuid, "VoucherAndCreditCards", 1000.00),
    CompanyBenefit(uuid, "NonCashBenefit", 1000.00)
  )

}
