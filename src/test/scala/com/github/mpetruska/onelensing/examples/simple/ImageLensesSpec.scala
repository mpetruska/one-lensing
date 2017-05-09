package com.github.mpetruska.onelensing.examples.simple

import com.github.mpetruska.onelensing.Aliases._
import com.github.mpetruska.onelensing.Lensing
import org.scalatest.{Matchers, WordSpec}

import scala.language.postfixOps

import scalaz.Id.Id
import scalaz.Lens._

class ImageLensesSpec extends WordSpec with Matchers {

  "ImageLenses" should {
    import ImageLensesSpec._

    val image = new CaseClassImageLenses[Id, Image](lensId, Lensing)

    "support the implementation of put greens" in {
      def addGreenOverlay(x: Seq[Seq[Int]]) = x.map(_.map(_ => 255))

      val greenOverlay = addGreenOverlay(image.pixels.green.get(original))

      image.pixels.green.set(original, greenOverlay) shouldBe allGreens
    }

    "support the implementation of removing red" in {
      image.pixels.red.mod(_.map(_.map(_ => 0)), original) shouldBe redRemoved
    }

    "support the implementation of enlarge" in {
      def enlargePixels(x: Seq[Seq[Pixel]]) = {
        x.flatMap { row =>
          val enlargedRow = row.flatMap(p => Seq(p, p))
          Seq(enlargedRow, enlargedRow)
        }
      }

      (for {
        _ <- image.pixelsLens %== enlargePixels
        _ <- image.width      %== (2 *)
        _ <- image.height     %== (2 *)
      } yield unit) exec original shouldBe enlarged
    }

  }

}

object ImageLensesSpec {

  val black  = Pixel(red =   0, green =   0, blue =   0)
  val red    = Pixel(red = 255, green =   0, blue =   0)
  val green  = Pixel(red =   0, green = 255, blue =   0)
  val yellow = Pixel(red = 255, green = 255, blue =   0)
  val blue   = Pixel(red =   0, green =   0, blue = 255)
  val cyan   = Pixel(red =   0, green = 255, blue = 255)

  val original = Image(2, 2, Seq(
    Seq(green, blue),
    Seq(red,   green)
  ))

  val allGreens = Image(2, 2, Seq(
    Seq(green,  cyan),
    Seq(yellow, green)
  ))

  val redRemoved = Image(2, 2, Seq(
    Seq(green, blue ),
    Seq(black, green)
  ))

  val enlarged = Image(4, 4, Seq(
    Seq(green, green, blue,  blue ),
    Seq(green, green, blue,  blue ),
    Seq(red,   red,   green, green),
    Seq(red,   red,   green, green)
  ))

}
