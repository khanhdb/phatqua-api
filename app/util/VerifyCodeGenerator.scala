package util

import scala.util.Random

object VerifyCodeGenerator {
  val FIXED_SIZE = 4
  def next(): String = {
    s"SD-${generateRandomNumber(FIXED_SIZE)}"
  }

  private def generateRandomNumber(size: Int): Int = {
    (0 until size).map{ i =>
      val r = if (i == size - 1) Random.between(1, 10) else Random.between(0, 10)
      r * Math.pow(10, i)
    }.sum.toInt
  }

}
