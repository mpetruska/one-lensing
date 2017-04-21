package com.github.mpetruska.onelensing.models

import java.time.OffsetDateTime

case class User(
  username:       String,
  firstName:      Option[String],
  lastName:       Option[String],
  emailAddresses: EmailAddresses,
  accesses:       Accesses,
  profiles:       Seq[Profile]
)

case class EmailAddresses(
  primaryEmailAddress: String,
  otherEmailAddresses: Seq[String]
)

case class Accesses(
  successfulLogins: Seq[OffsetDateTime],
  failedAttempts:   Seq[OffsetDateTime]
)

case class Profile(
  profileName: String,
  settings:    Map[String, String]
)
