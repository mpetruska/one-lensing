package com.github.mpetruska.onelensing

import scala.language.higherKinds

import scalaz.Id.Id

trait LensesBase[L[F[_], _, _], DataType] {

  def baseLens: L[Id, DataType, DataType]

}
