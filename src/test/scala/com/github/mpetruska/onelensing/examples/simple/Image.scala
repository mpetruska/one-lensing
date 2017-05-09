package com.github.mpetruska.onelensing.examples.simple

case class Image(width: Int, height: Int, pixels: Seq[Seq[Pixel]])

case class Pixel(red: Int, green: Int, blue: Int)
