package com.github.mpetruska.onelensing

import java.time.OffsetDateTime

import com.github.mpetruska.onelensing.LensHelper._

import scala.annotation.implicitNotFound
import scala.language.higherKinds
import scalaz._

@implicitNotFound("No member of type class UserLenses in scope for ${A}")
trait UserLenses[F[_], DataType, A] extends LensesBase[UserLenses, DataType] {

  type EmailAddresses
  type Accesses
  type Profile

  def username: A @> F[String]
  def firstName: A @> F[Option[String]]
  def lastName: A @> F[Option[String]]

  def emailAddressesLens: A @> F[EmailAddresses]
  def emailAddresses: EmailAddressesLenses[F, EmailAddresses, A]

  def accessesLens: A @> F[Accesses]
  def accesses: AccessesLenses[F, Accesses, A]

  def profilesLens: A @> F[Seq[Profile]]
  def profiles: ProfileLenses[FSeq[F]#λ, Profile, A]

}

@implicitNotFound("No member of type class EmailAddressesLenses in scope for ${A}")
trait EmailAddressesLenses[F[_], DataType, A] extends LensesBase[EmailAddressesLenses, DataType] {

  def primaryEmailAddress: A @> F[String]
  def otherEmailAddresses: A @> F[Seq[String]]

}

@implicitNotFound("No member of type class AccessesLenses in scope for ${A}")
trait AccessesLenses[F[_], DataType, A] extends LensesBase[AccessesLenses, DataType] {

  def successfulLogins: A @> F[Seq[OffsetDateTime]]
  def failedAttempts: A @> F[Seq[OffsetDateTime]]

}

@implicitNotFound("No member of type class ProfileLenses in scope for ${A}")
trait ProfileLenses[F[_], DataType, A] extends LensesBase[ProfileLenses, DataType] {

  def profileName: A @> F[String]

  def settingA: A @> F[String]
  def settingB: A @> F[Boolean]

}
