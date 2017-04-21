package com.github.mpetruska.onelensing.models

import java.time.OffsetDateTime

import com.github.mpetruska.onelensing._
import com.github.mpetruska.onelensing.LensHelper._

import scala.language.higherKinds
import scala.util.Try

import scalaz._
import scalaz.Id.Id
import scalaz.Lens._

object CaseClassDefaults {

  lazy val defaultUser = User(
    username = "",
    firstName = None,
    lastName = None,
    emailAddresses = defaultEmailAddresses,
    accesses = defaultAccesses,
    profiles = Seq.empty
  )

  lazy val defaultEmailAddresses = EmailAddresses(primaryEmailAddress = "", otherEmailAddresses = Seq.empty)
  lazy val defaultAccesses = Accesses(Seq.empty, Seq.empty)
  lazy val defaultProfile = Profile(profileName = "", settings = Map.empty)

}

import CaseClassDefaults._

class CaseClassUserLenses[F[_], A](base: A @> F[User], lensing: Lensing[F]) extends UserLenses[F, User, A] {

  type EmailAddresses = models.EmailAddresses
  type Accesses = models.Accesses
  type Profile = models.Profile

  lazy val baseLens: UserLenses[Id, User, User] =
    new CaseClassUserLenses[Id, User](lensId, Lensing)

  lazy val username: A @> F[String]          = base >=> lensing.lift(lensg(a => b => a.copy(username = b),  _.username),  defaultUser)
  lazy val firstName: A @> F[Option[String]] = base >=> lensing.lift(lensg(a => b => a.copy(firstName = b), _.firstName), defaultUser)
  lazy val lastName: A @> F[Option[String]]  = base >=> lensing.lift(lensg(a => b => a.copy(lastName = b),  _.lastName),  defaultUser)

  lazy val emailAddressesLens: A @> F[EmailAddresses] =
    base >=> lensing.lift(lensg(a => b => a.copy(emailAddresses = b), _.emailAddresses), defaultUser)
  lazy val emailAddresses: EmailAddressesLenses[F, EmailAddresses, A] =
    new CaseClassEmailAddressesLenses[F, A](emailAddressesLens, lensing)

  lazy val accessesLens: A @> F[Accesses] =
    base >=> lensing.lift(lensg(a => b => a.copy(accesses = b), _.accesses), defaultUser)
  lazy val accesses: AccessesLenses[F, Accesses, A] =
    new CaseClassAccessesLenses[F, A](accessesLens, lensing)

  lazy val profilesLens: A @> F[Seq[Profile]] =
    base >=> lensing.lift(lensg(a => b => a.copy(profiles = b), _.profiles), defaultUser)
  lazy val profiles: ProfileLenses[FSeq[F]#λ, Profile, A] =
    new CaseClassProfileLenses[FSeq[F]#λ, A](profilesLens, lensing.seq)

}

class CaseClassEmailAddressesLenses[F[_], A](base: A @> F[EmailAddresses], lensing: Lensing[F]) extends EmailAddressesLenses[F, EmailAddresses, A] {

  lazy val baseLens: EmailAddressesLenses[Id, EmailAddresses, EmailAddresses] =
    new CaseClassEmailAddressesLenses[Id, EmailAddresses](lensId, Lensing)

  lazy val primaryEmailAddress: A @> F[String] =
    base >=> lensing.lift(lensg(a => b => a.copy(primaryEmailAddress = b), _.primaryEmailAddress), defaultEmailAddresses)

  lazy val otherEmailAddresses: A @> F[Seq[String]] =
    base >=> lensing.lift(lensg(a => b => a.copy(otherEmailAddresses = b), _.otherEmailAddresses), defaultEmailAddresses)

}

class CaseClassAccessesLenses[F[_], A](base: A @> F[Accesses], lensing: Lensing[F]) extends AccessesLenses[F, Accesses, A] {

  lazy val baseLens: AccessesLenses[Id, Accesses, Accesses] =
    new CaseClassAccessesLenses[Id, Accesses](lensId, Lensing)

  lazy val successfulLogins: A @> F[Seq[OffsetDateTime]] =
    base >=> lensing.lift(lensg(a => b => a.copy(successfulLogins = b), _.successfulLogins), defaultAccesses)

  lazy val failedAttempts: A @> F[Seq[OffsetDateTime]] =
    base >=> lensing.lift(lensg(a => b => a.copy(failedAttempts = b), _.failedAttempts), defaultAccesses)

}

class CaseClassProfileLenses[F[_], A](base: A @> F[Profile], lensing: Lensing[F]) extends ProfileLenses[F, Profile, A] {

  lazy val baseLens: ProfileLenses[Id, Profile, Profile] =
    new CaseClassProfileLenses[Id, Profile](lensId, Lensing)

  lazy val profileName: A @> F[String] = base >=> lensing.lift(lensg(a => b => a.copy(profileName = b), _.profileName), defaultProfile)

  lazy val keyA = "a"
  lazy val keyB = "b"

  lazy val settings: Profile @> Map[String, String] = lensg(a => b => a.copy(settings = b), _.settings)
  lazy val withDefault: Map[String, String] @> Map.WithDefault[String, String] =
    lensg(
      set = _ => b => b,
      get = _.withDefault {
        case `keyA` => "someDefault"
        case `keyB` => "true"
        case _      => ""
      }.asInstanceOf[Map.WithDefault[String, String]]
    )

  def asBoolean(default: Boolean): String @> Boolean =
    lensg(
      set = _ => b => b.toString,
      get = a => Try(a.toBoolean) getOrElse default
    )

  lazy val settingA: A @> F[String]  =
    base >=> lensing.lift(settings >=> withDefault >=> mapWithDefaultLens(keyA), defaultProfile)

  lazy val settingB: A @> F[Boolean] =
    base >=> lensing.lift(settings >=> withDefault >=> mapWithDefaultLens(keyB) >=> asBoolean(true), defaultProfile)

}
