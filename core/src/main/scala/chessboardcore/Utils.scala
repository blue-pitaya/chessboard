package chessboardcore

import cats.effect.IO
import scala.util.Random

object Utils {

  def createId(): IO[String] = IO(
    (0 until 32).foldLeft("") { case (acc, _) =>
      acc ++ Random.nextInt(16).toHexString
    }
  )

}