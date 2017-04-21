package com.github.mpetruska.onelensing

import scala.language.higherKinds

import LensHelper._

import scalaz._
import Id.Id
import Lens._
import syntax.std.all._

trait Lensing[F[_]] {

  def lift[A, B](lens: A @> B, default: => A): F[A] @> F[B]

  def option: OptionLensing[F] = new OptionLensing[F](this)
  def seq: SeqLensing[F] = new SeqLensing[F](this)

}

object Lensing extends Lensing[Id] {

  def lift[A, B](lens: A @> B, default: => A): Id[A] @> Id[B] = lens

}

class OptionLensing[F[_]](flensing: Lensing[F]) extends Lensing[FOption[F]#λ] {

  def lift[A, B](lens: A @> B, default: => A): F[Option[A]] @> F[Option[B]] =
    flensing.lift(lensg(
      set = aoption => boption => boption.fold[Option[A]](None)(b => lens.set(aoption getOrElse default, b).some),
      get = _.map(lens.get)
    ), None)

}

class SeqLensing[F[_]](flensing: Lensing[F]) extends Lensing[FSeq[F]#λ] {

  def lift[A, B](lens: A @> B, default: => A): F[Seq[A]] @> F[Seq[B]] =
    flensing.lift(lensg(
      set = as => bs =>
        as.padTo(bs.length, default)
          .zip(bs)
          .map((lens.set _).tupled),
      get = _.map(lens.get)
    ), Seq.empty)

}
