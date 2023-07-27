package example.pages.game

import org.scalajs.dom
import chessboardcore.Utils
import cats.effect.SyncIO

object PlayerService {
  type PlayerId = String

  private val playerIdKey = "myId"
  private val tokenKey = "myToken"

  def createOrLoadToken(): SyncIO[String] = createOrLoadId(tokenKey)

  def createOfLoadPlayerId(): SyncIO[String] = createOrLoadId(playerIdKey)

  private def createOrLoadId(key: String): SyncIO[String] = for {
    idOpt <- load(key)
    id <- idOpt match {
      case Some(id) => SyncIO.pure(id)
      case None     => createAndSave(key)
    }
  } yield (id)

  private def load(key: String): SyncIO[Option[String]] =
    SyncIO(Option(dom.window.localStorage.getItem(key)))

  private def createAndSave(key: String): SyncIO[String] = for {
    id <- Utils.createId[SyncIO]()
    _ <- SyncIO(dom.window.localStorage.setItem(key, id))
  } yield (id)

}
