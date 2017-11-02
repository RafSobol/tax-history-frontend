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

package controllers

import javax.inject.Inject

import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{CitizenDetailsConnector, TaxHistoryConnector}
import form.SelectClientForm.selectClientForm
import model.api.{Allowance, Employment}
import models.taxhistory.Person
import play.api.i18n.MessagesApi
import play.api.mvc._
import play.api.{Configuration, Environment, Logger}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.time.TaxYearResolver
import uk.gov.hmrc.urls.Link

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EmploymentSummaryController @Inject()(
                                             val taxHistoryConnector: TaxHistoryConnector,
                                             val citizenDetailsConnector: CitizenDetailsConnector,
                                             override val authConnector: FrontendAuthConnector,
                                             override val config: Configuration,
                                             override val env: Environment,
                                             implicit val messagesApi: MessagesApi
                                           ) extends BaseController {

  def getTaxHistory() = Action.async {
    implicit request => {
      val maybeNino = request.session.get("USER_NINO").map(Nino(_))
      authorisedForAgent {
        maybeNino match {
          case Some(nino) => {
            for {maybePerson <- retrieveCitizenDetails(nino)
                 taxHistoryResponse <- renderTaxHistoryPage(nino, maybePerson)}
              yield {
                taxHistoryResponse
              }
          }
          case None => {
            Logger.warn("No nino supplied.")
            val navLink = Link.toInternalPage(
              url = FrontendAppConfig.AfiHomePage,
              value = Some(messagesApi("employmenthistory.afihomepage.linktext"))).toHtml
            Future.successful(Ok(views.html.taxhistory.select_client(selectClientForm, Some(navLink))))
          }
        }
      }
    }
  }

  private def retrieveCitizenDetails(ninoField: Nino)
                            (implicit hc: HeaderCarrier, request: Request[_]): Future[Either[Int, Person]] = {
    val details = {
      citizenDetailsConnector.getPersonDetails(ninoField) map {
        personResponse =>
          personResponse.status match {
            case OK => {
              val person = personResponse.json.as[Person]
              println()
              if (person.deceased) Left(GONE) else Right(person)
            }
            case status => Left(status)
          }
      }
    }.recoverWith {
      case _ => Future.successful(Left(BAD_REQUEST))
    }
    details
  }

  private def renderTaxHistoryPage(ninoField: Nino, maybePerson: Either[Int, Person])
                          (implicit hc: HeaderCarrier, request: Request[_]): Future[Result] = {
    maybePerson match {
      case Left(status) => status match {
        case LOCKED => Future.successful(Redirect(controllers.routes.ClientErrorController.getMciRestricted()))
        case GONE => Future.successful(Redirect(controllers.routes.ClientErrorController.getDeceased()))
        case _ => Future.successful(Redirect(controllers.routes.ClientErrorController.getTechnicalError()))
      }
      case Right(person) => retrieveTaxHistoryData(ninoField, Some(person))
    }
  }


  private def retrieveTaxHistoryData(ninoField: Nino, person: Option[Person])
                            (implicit hc: HeaderCarrier, request: Request[_]): Future[Result] = {
    val cy1 = TaxYearResolver.currentTaxYear - 1
    taxHistoryConnector.getEmploymentsAndPensions(ninoField, cy1) flatMap { empResponse =>
      empResponse.status match {
        case OK => {
          taxHistoryConnector.getAllowances(ninoField, cy1) map { allowanceResponse =>
            allowanceResponse.status match {
              case OK | NOT_FOUND =>
                val employments = empResponse.json.as[List[Employment]]
                val allowances = allowanceResponse.json.as[List[Allowance]]
                val sidebarLink = Link.toInternalPage(
                  url = FrontendAppConfig.AfiHomePage,
                  value = Some(messagesApi("employmenthistory.afihomepage.linktext")),
                  id= Some("back-link")
                ).toHtml

                Ok(views.html.taxhistory.employment_summary(ninoField.nino, cy1,
                  employments, allowances, person, Some(sidebarLink)))//.removingFromSession("USER_NINO")
              case status => handleHttpFailureResponse(status, ninoField)
            }
          }
        }
        case status => Future.successful(handleHttpFailureResponse(status, ninoField))
      }
    }
  }
}