package com.github.mpetruska.onelensing.examples.simple

import com.github.mpetruska.onelensing._
import com.github.mpetruska.onelensing.LensHelper._

import scala.annotation.implicitNotFound
import scala.language.higherKinds

import scalaz._
import scalaz.Id.Id
import scalaz.Lens._

@implicitNotFound("No member of type class ImageLenses in scope for ${A}")
trait ImageLenses[F[_], DataType, A] extends LensesBase[ImageLenses, DataType] {

  type PixelType

  def width: A @> F[Int]
  def height: A @> F[Int]

  def pixelsLens: A @> F[Seq[Seq[PixelType]]]
  def pixels: PixelLenses[FSeq[FSeq[F]#λ]#λ, PixelType, A]

}

@implicitNotFound("No member of type class PixelLenses in scope for ${A}")
trait PixelLenses[F[_], DataType, A] extends LensesBase[PixelLenses, DataType] {

  def red: A @> F[Int]
  def green: A @> F[Int]
  def blue: A @> F[Int]

}

class CaseClassImageLenses[F[_], A](base: A @> F[Image], lensing: Lensing[F]) extends ImageLenses[F, Image, A] {

  type PixelType = Pixel

  lazy val defaultImage = Image(0, 0, Seq.empty)

  def baseLens: ImageLenses[Id, Image, Image] = new CaseClassImageLenses[Id, Image](lensId, Lensing)

  def width: A @> F[Int]  = base >=> lensing.lift(lensg(a => b => a.copy(width  = b), _.width) , defaultImage)
  def height: A @> F[Int] = base >=> lensing.lift(lensg(a => b => a.copy(height = b), _.height), defaultImage)

  def pixelsLens: A @> F[Seq[Seq[PixelType]]] = base >=> lensing.lift(lensg(a => b => a.copy(pixels = b), _.pixels), defaultImage)
  def pixels: PixelLenses[FSeq[FSeq[F]#λ]#λ, PixelType, A] =
    new CaseClassPixelLenses[FSeq[FSeq[F]#λ]#λ, A](pixelsLens, lensing.seq.seq)

}

class CaseClassPixelLenses[F[_], A](base: A @> F[Pixel], lensing: Lensing[F]) extends PixelLenses[F, Pixel, A] {

  lazy val defaultPixel = Pixel(0, 0, 0) // black

  def baseLens: PixelLenses[Id, Pixel, Pixel] = new CaseClassPixelLenses[Id, Pixel](lensId, Lensing)

  def red: A @> F[Int]   = base >=> lensing.lift(lensg(a => b => a.copy(red   = b), _.red)  , defaultPixel)
  def green: A @> F[Int] = base >=> lensing.lift(lensg(a => b => a.copy(green = b), _.green), defaultPixel)
  def blue: A @> F[Int]  = base >=> lensing.lift(lensg(a => b => a.copy(blue  = b), _.blue) , defaultPixel)

}
