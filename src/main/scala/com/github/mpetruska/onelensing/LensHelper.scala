package com.github.mpetruska.onelensing

import scala.language.higherKinds

object LensHelper {

  type FOption[F[_]] = { type λ[A] = F[Option[A]] }
  type FSeq[F[_]] = { type λ[A] = F[Seq[A]] }

}
