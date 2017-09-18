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

package form

import models.taxhistory.SelectClient
import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import uk.gov.hmrc.domain.Nino.isValid

object SelectClientForm {
  val selectClientForm: Form[SelectClient] = {
    Form(mapping(
      "clientId" -> text
        .verifying("selectclient.error.empty", isNonEmpty(_))
        .verifying("selectclient.error.invalid-length", text => if(isNonEmpty(text)) isValidLength(text) else true )
        .verifying("selectclient.error.invalid-format",text => if(isNonEmpty(text) && isValidLength(text)) isValidNino(text) else true)
    )(SelectClient.apply)(SelectClient.unapply))
  }

  private def isNonEmpty(nino: String):    Boolean = nino.nonEmpty
  private def isValidLength(nino: String): Boolean = nino.length == 9
  private def isValidNino(nino: String):   Boolean = isValid(nino)
}