package chessboardcore

import cats.effect.kernel.Sync

import scala.util.Random

object Utils {

  def createId[F[_]: Sync](): F[String] = Sync[F].delay(unsafeCreateId())

  def unsafeCreateId(): String = (0 until 32).foldLeft("") { case (acc, _) =>
    acc ++ Random.nextInt(16).toHexString
  }

}
