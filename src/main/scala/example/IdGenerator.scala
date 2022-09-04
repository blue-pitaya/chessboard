package example

import scala.util.Random

object IdGenerator {
  // 1 in 16 777 216 chance of duplicate
  val length = 4
  def nextId = Random.alphanumeric.take(length).toList.mkString
}
