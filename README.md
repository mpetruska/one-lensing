[![Build Status](https://travis-ci.org/mpetruska/one-lensing.svg?branch=master)](https://travis-ci.org/mpetruska/uk-modulo-scala)

One-lensing data abstraction mini-library
=========================================

In most applications the "domain" logic is usually implemented with the help of
the data structures and values that describe the given problems domain. Unfortunately
there are cases when the developer does not have full control of the data types
used to model the domain (e.g., generated code, persistence library requirements).

If there's such restriction in place developers usually have two options on how to
implement the application logic:

1. _Work directly with the types that adhere to the constraints given (e.g. use
   generated code)._
   
2. _Create the data types that support the application logic better, and also maintain
   mapping code that translates between the set of types that fulfill the constraints._

In order to reach to a third option, we need to think about this:  
Application logic does not need to explicitly restrict the data structures it works with;
it only needs to be able to access the atomic values that the structure holds. Here "access"
means the ability to extract the value and update it. Lenses naturally lend themselves to
solve this problem; hence the third option:

**_Write application logic in a way that it accesses the data values through Lenses._**

This option has the following benefits:
- allows re-shaping data structures
- less boilerplate in most cases

Example
-------

Let's imagine we want to implement application logic that works with the pixels of images.
We want the code to not rely on one exact data type, but still be able to access pixel data.
Here is a useful lens abstraction to work with:

```Scala
import com.github.mpetruska.onelensing._
import com.github.mpetruska.onelensing.LensHelper._

import scala.annotation.implicitNotFound
import scala.language.higherKinds

import scalaz._

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
```

This abstraction will allow us to access the image data without actually knowing the underlying
data type:

```Scala
import scalaz.Id.Id

val orinal: A
val image: ImageLenses[Id, A, A]

def addGreenOverlay(x: Seq[Seq[Int]]): Seq[Seq[Int]] = x.map(_.map(_ => 255))

val greenOverlay: Seq[Seq[Int]] = addGreenOverlay(
  image.pixels.green.get(original))

val greenified = image.pixels.green.set(original, greenOverlay)
```

Scalaz lenses also have additional useful features, here is an example of implementing a
State computation on top of lenses:

```Scala
def enlargePixels[P](x: Seq[Seq[P]]): Seq[Seq[P]] = {
  x.flatMap { row =>
    val enlargedRow = row.flatMap(p => Seq(p, p))
    Seq(enlargedRow, enlargedRow)
  }
}

(for {
  _ <- image.pixelsLens %== enlargePixels
  _ <- image.width      %== (2 *)
  _ <- image.height     %== (2 *)
} yield unit) exec original
```
