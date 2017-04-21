package com.github.mpetruska.onelensing

import org.scalatest.{Matchers, WordSpec}

import scalaz.Id.Id

trait UserLensesSpec[A] extends WordSpec with Matchers {

  val username = "user1"
  val emailAddress1 = "us@er.one"
  val emailAddress2 = "us@er.two"
  val emailAddress3 = "us@er.three"

  def implementationName: String

  def defaultUser: A
  implicit def userLenses: UserLenses[Id, A, A]

  def expectedRegistered: A
  def expectedEmailAddress2Added: A
  def expectedEmailAddress3Added: A

  s"UserLenses implementation `$implementationName`" should {
    import UserApplicationLogic._

    "register user correctly" in {
      register(username, emailAddress1, defaultUser) shouldBe expectedRegistered
    }

    "add email addresses correctly" in {
      val emailAddress2Added = addEmailAddress(expectedRegistered, emailAddress2, isPrimary = true)
      emailAddress2Added shouldBe expectedEmailAddress2Added

      addEmailAddress(emailAddress2Added, emailAddress3, isPrimary = true) shouldBe expectedEmailAddress3Added
    }

  }

}
