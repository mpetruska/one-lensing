package com.github.mpetruska.onelensing.models

import com.github.mpetruska.onelensing._

import scalaz.Id.Id
import scalaz.Lens._

class CaseClassUserLensesSpec extends UserLensesSpec[User] {

  def implementationName: String = "CaseClassUserLenses"

  val defaultUser: User = CaseClassDefaults.defaultUser
  val userLenses: UserLenses[Id, User, User] = new CaseClassUserLenses[Id, User](lensId, Lensing)

  val expectedRegistered: User = defaultUser.copy(
    username = username,
    emailAddresses = EmailAddresses(primaryEmailAddress = emailAddress1, otherEmailAddresses = Seq.empty)
  )

  val expectedEmailAddress2Added: User = expectedRegistered.copy(
    emailAddresses = EmailAddresses(primaryEmailAddress = emailAddress2, otherEmailAddresses = Seq(emailAddress1))
  )

  val expectedEmailAddress3Added: User = expectedRegistered.copy(
    emailAddresses = EmailAddresses(primaryEmailAddress = emailAddress3, otherEmailAddresses = Seq(emailAddress2, emailAddress1))
  )

}
