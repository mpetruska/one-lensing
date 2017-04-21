package com.github.mpetruska.onelensing

import org.scalatest.{Matchers, WordSpec}

import scalaz._
import Lens._
import syntax.std.all._

object LensingSpec {

  case class A(id: String = "", option: Option[B] = None, seq: Seq[B] = Seq.empty)
  case class B(id: String = "", option: Option[C] = None, seq: Seq[C] = Seq.empty)
  case class C(id: String = "", option: Option[D] = None, seq: Seq[D] = Seq.empty)
  case class D(id: String)

  val optionAB: A @> Option[B] = lensg(a => b => a.copy(option = b), _.option)
  val seqAB:    A @> Seq[B]    = lensg(a => b => a.copy(seq = b),    _.seq)

  val optionBC: B @> Option[C] = lensg(a => b => a.copy(option = b), _.option)
  val seqBC:    B @> Seq[C]    = lensg(a => b => a.copy(seq = b),    _.seq)

  val optionCD: C @> Option[D] = lensg(a => b => a.copy(option = b), _.option)
  val seqCD:    C @> Seq[D]    = lensg(a => b => a.copy(seq = b),    _.seq)

  val optionOptionAC: A @> Option[Option[C]] = optionAB >=> Lensing.option.lift(optionBC, B())
  val optionSeqAC:    A @> Option[Seq[C]]    = optionAB >=> Lensing.option.lift(seqBC, B())
  val seqOptionAC:    A @> Seq[Option[C]]    = seqAB    >=> Lensing.seq.lift(optionBC, B())
  val seqSeqAC:       A @> Seq[Seq[C]]       = seqAB    >=> Lensing.seq.lift(seqBC, B())

  val (c1, c2) = (C("1"), C("2"))
  val b1 = B("one",  None,     Seq(c1))
  val b2 = B("twos", Some(c1), Seq(c2, c2))

  val (d1, d2) = (D("1"), D("2"))

}

class LensingSpec extends WordSpec with Matchers {

  import LensingSpec._

  "Lensing" should {

    // two-way

    "2-way combine Option and Option correctly" in {
      optionOptionAC.get(A())                                shouldBe None
      optionOptionAC.get(A("a", B("b").some))                shouldBe Some(None)
      optionOptionAC.get(A("a", B("b", Some(C("c"))).some))  shouldBe Some(Some(C("c")))

      optionOptionAC.set(A(), None)            shouldBe A()
      optionOptionAC.set(A(), Some(None))      shouldBe A(option = Some(B()))
      optionOptionAC.set(A(), Some(Some(C()))) shouldBe A(option = Some(B("", Some(C()))))
    }

    "2-way combine Option and Seq    correctly" in {
      optionSeqAC.get(A())                                    shouldBe None
      optionSeqAC.get(A("a", B("b").some))                    shouldBe Some(Seq.empty)
      optionSeqAC.get(A("a", B("b", None, Seq(c1, c2)).some)) shouldBe Some(Seq(c1, c2))

      optionSeqAC.set(A(), None)              shouldBe A()
      optionSeqAC.set(A(), Some(Seq.empty))   shouldBe A(option = Some(B()))
      optionSeqAC.set(A(), Some(Seq(c1, c2))) shouldBe A(option = Some(B(seq = Seq(c1, c2))))
    }

    "2-way combine Seq    and Option correctly" in {
      seqOptionAC.get(A())                       shouldBe Seq.empty
      seqOptionAC.get(A("a", seq = Seq(b1, b2))) shouldBe Seq(None, Some(c1))

      seqOptionAC.set(A(), Seq.empty)           shouldBe A()
      seqOptionAC.set(A(), Seq(None, Some(c1))) shouldBe A(seq = Seq(B(), B(option = Some(c1))))
    }

    "2-way combine Seq    and Seq    correctly" in {
      seqSeqAC.get(A())                       shouldBe Seq.empty
      seqSeqAC.get(A("a", seq = Seq(b1, b2))) shouldBe Seq(Seq(c1), Seq(c2, c2))

      seqSeqAC.set(A(), Seq.empty)                 shouldBe A()
      seqSeqAC.set(A(), Seq(Seq(c1), Seq(c2, c2))) shouldBe A(seq = Seq(
        B(seq = Seq(c1)),
        B(seq = Seq(c2, c2))
      ))
    }

    // three-way

    "3-way combine Option, Option and Option correctly" in {
      val ad: A @> Option[Option[Option[D]]] = optionOptionAC >=> Lensing.option.option.lift(optionCD, C())

      ad.get(A())                                             shouldBe None
      ad.get(A("a", B("b").some))                             shouldBe Some(None)
      ad.get(A("a", B("b", Some(c1)).some))                   shouldBe Some(Some(None))
      ad.get(A("a", B("b", Some(C("c", Some(D("d"))))).some)) shouldBe Some(Some(Some(D("d"))))

      ad.set(A(), None)                     shouldBe A()
      ad.set(A(), Some(None))               shouldBe A(option = B().some)
      ad.set(A(), Some(Some(None)))         shouldBe A(option = B(option = Some(C())).some)
      ad.set(A(), Some(Some(Some(D("d"))))) shouldBe A(option = B(option = Some(C(option = Some(D("d"))))).some)
    }

    "3-way combine Option, Option and Seq    correctly" in {
      val ad: A @> Option[Option[Seq[D]]] = optionOptionAC >=> Lensing.option.option.lift(seqCD, C())

      ad.get(A())                                                  shouldBe None
      ad.get(A("a", B("b").some))                                  shouldBe Some(None)
      ad.get(A("a", B("b", Some(c1)).some))                        shouldBe Some(Some(Seq.empty))
      ad.get(A("a", B("b", Some(C("c", seq = Seq(d1, d2)))).some)) shouldBe Some(Some(Seq(d1, d2)))

      ad.set(A(), None)                    shouldBe A()
      ad.set(A(), Some(None))              shouldBe A(option = B().some)
      ad.set(A(), Some(Some(Seq.empty)))   shouldBe A(option = B(option = Some(C())).some)
      ad.set(A(), Some(Some(Seq(d1, d2)))) shouldBe A(option = B(option = Some(C(seq = Seq(d1, d2)))).some)
    }

    "3-way combine Option, Seq    and Option correctly" in {
      val ad: A @> Option[Seq[Option[D]]] = optionSeqAC >=> Lensing.option.seq.lift(optionCD, C())

      ad.get(A())                 shouldBe None
      ad.get(A("a", B("b").some)) shouldBe Some(Seq.empty)
      ad.get(A("a", b1.some))     shouldBe Some(Seq(None))
      ad.get(
        A("a", B("b", seq = Seq(
          C("c"),
          C("c2", Some(d1)),
          C("c3", Some(d2))
        )).some)) shouldBe Some(Seq(None, Some(d1), Some(d2)))

      ad.set(A(), None)                               shouldBe A()
      ad.set(A(), Seq.empty.some)                     shouldBe A(option = B().some)
      ad.set(A(), Seq(Some(d1), None, Some(d2)).some) shouldBe
        A(option = B(seq = Seq(
          C(option = d1.some),
          C(),
          C(option = d2.some)
        )).some)
    }

    "3-way combine Option, Seq    and Seq    correctly" in {
      val ad: A @> Option[Seq[Seq[D]]] = optionSeqAC >=> Lensing.option.seq.lift(seqCD, C())

      ad.get(A())            shouldBe None
      ad.get(A("a", B("b").some)) shouldBe Seq.empty.some
      ad.get(A("a", B("b", seq = Seq(
        C("0"),
        C("1", seq = Seq(d1)),
        C("2", seq = Seq(d2, d2))
      )).some)) shouldBe Seq(Seq.empty, Seq(d1), Seq(d2, d2)).some

      ad.set(A(), None)                                      shouldBe A()
      ad.set(A(), Seq.empty.some)                            shouldBe A(option = B().some)
      ad.set(A(), Seq(Seq(d1), Seq.empty, Seq(d2, d2)).some) shouldBe
        A(option = B(seq = Seq(
          C(seq = Seq(d1)),
          C(),
          C(seq = Seq(d2, d2))
        )).some)
    }

    "3-way combine Seq,    Option and Option correctly" in {
      val ad: A @> Seq[Option[Option[D]]] = seqOptionAC >=> Lensing.seq.option.lift(optionCD, C())

      ad.get(A()) shouldBe Seq.empty
      ad.get(A("a", seq = Seq(
        B("0"),
        B("0", C().some),
        B("1", C("2", Some(d1)).some)
      ))) shouldBe Seq(None, Some(None), Some(d1.some))

      ad.set(A(), Seq.empty)                            shouldBe A()
      ad.set(A(), Seq(None, Some(None), Some(d1.some))) shouldBe
        A(seq = Seq(
          B(),
          B(option = C().some),
          B(option = C(option = d1.some).some)
        ))
    }

    "3-way combine Seq,    Option and Seq    correctly" in {
      val ad: A @> Seq[Option[Seq[D]]] = seqOptionAC >=> Lensing.seq.option.lift(seqCD, C())

      ad.get(A()) shouldBe Seq.empty
      ad.get(A("a", seq = Seq(
        B("0"),
        B("0", C("0").some),
        B("1", C("1", seq = Seq(d1)).some),
        B("2", C("2", seq = Seq(d2, d2)).some)
      ))) shouldBe Seq(
        None,
        Seq.empty.some,
        Seq(d1).some,
        Seq(d2, d2).some
      )

      ad.set(A(), Seq.empty) shouldBe A()
      ad.set(A(), Seq(
        Seq(d1).some,
        Seq.empty.some,
        Seq(d2, d2).some,
        None)
      ) shouldBe A(seq = Seq(
        B(option = C(seq = Seq(d1)).some),
        B(option = C().some),
        B(option = C(seq = Seq(d2, d2)).some),
        B()
      ))
    }

    "3-way combine Seq,    Seq    and Option correctly" in {
      val ad: A @> Seq[Seq[Option[D]]] = seqSeqAC >=> Lensing.seq.seq.lift(optionCD, C())

      ad.get(A()) shouldBe Seq.empty
      ad.get(
        A("a",
          seq = Seq(
            B("0"),
            B("0", seq = Seq(C("0"))),
            B("1", seq = Seq(C("1", d1.some))),
            B("2", seq = Seq(C("1", d2.some), C("0")))
        ))) shouldBe Seq(
          Seq.empty,
          Seq(None),
          Seq(d1.some),
          Seq(d2.some, None)
      )

      ad.set(A(), Seq.empty) shouldBe A()
      ad.set(A(), Seq(
        Seq(d2.some, None, d2.some),
        Seq.empty,
        Seq(d1.some),
        Seq(None)
      )) shouldBe
        A(seq = Seq(
          B(seq = Seq(C(option = d2.some), C(), C(option = d2.some))),
          B(),
          B(seq = Seq(C(option = d1.some))),
          B(seq = Seq(C()))
        ))
    }

    "3-way combine Seq,    Seq    and Seq    correctly" in {
      val ad: A @> Seq[Seq[Seq[D]]] = seqSeqAC >=> Lensing.seq.seq.lift(seqCD, C())

      ad.get(A()) shouldBe Seq.empty
      ad.get(A("a", seq = Seq(
        B("0"),
        B("0", seq = Seq(C("0"))),
        B("1", seq = Seq(C("1", seq = Seq(d1)))),
        B("2", seq = Seq(C("0"), C("1", seq = Seq(d1)), C("2", seq = Seq(d2, d2))))
      ))) shouldBe Seq(
        Seq.empty,
        Seq(Seq.empty),
        Seq(Seq(d1)),
        Seq(Seq.empty, Seq(d1), Seq(d2, d2))
      )

      ad.set(A(), Seq.empty) shouldBe A()
      ad.set(A(), Seq(
        Seq(Seq(d2, d2), Seq(d1), Seq.empty),
        Seq(Seq.empty),
        Seq.empty,
        Seq(Seq(d1))
      )) shouldBe A(seq = Seq(
        B(seq = Seq(C(seq = Seq(d2, d2)), C(seq = Seq(d1)), C())),
        B(seq = Seq(C())),
        B(),
        B(seq = Seq(C(seq = Seq(d1))))
      ))
    }

  }

}
