package com.github.mpetruska.onelensing

import com.github.mpetruska.onelensing.Aliases._

import scala.language.postfixOps

import scalaz._
import scalaz.Id.Id
import scalaz.syntax.std.all._

object UserApplicationLogic {

  def register[A](username: String, emailAddress: String, default: A)(implicit userLenses: UserLenses[Id, A, A]): A = {
    (for {
      _ <- userLenses.username := username
      _ <- userLenses.emailAddresses.primaryEmailAddress := emailAddress
    } yield unit) exec default
  }

  def addEmailAddress[A](user: A, emailAddress: String, isPrimary: Boolean)(implicit userLenses: UserLenses[Id, A, A]): A = {
    (for {
      primary    <- userLenses.emailAddresses.primaryEmailAddress
      others     <- userLenses.emailAddresses.otherEmailAddresses
      all        =  (primary +: others) :+ emailAddress
      newPrimary =  isPrimary.fold(emailAddress, primary)
      newOthers  =  all.distinct.filterNot(newPrimary ==)
      _          <- userLenses.emailAddresses.primaryEmailAddress := newPrimary
      _          <- userLenses.emailAddresses.otherEmailAddresses := newOthers
    } yield unit) exec user
  }

}
