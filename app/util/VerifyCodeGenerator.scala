package util

import scala.util.Random

object VerifyCodeGenerator {
  val FIXED_SIZE = 4

 def genCodes(total: Int, keys: List[String]): List[String] = {
   if (total == 0) {
     keys
   } else {
     genCodes(total - 1, next(keys) :: keys)
   }
 }
  def next(excludedKeys: List[String]): String = {
    val key = s"SD-${generateRandomNumber(FIXED_SIZE)}"
    if (excludedKeys.contains(key)){
      next(excludedKeys)
    } else {
      key
    }
  }

  private def generateRandomNumber(size: Int): String = {
    val n = (0 until size).map{ i =>
      val r = if (i == size - 1) Random.between(1, 10) else Random.between(0, 10)
      r * Math.pow(10, i)
    }.sum.toLong
    java.lang.Long.toString(n, 36).toUpperCase
  }


}
