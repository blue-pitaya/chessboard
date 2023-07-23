package example.pages.game

import org.scalajs.dom
import chessboardcore.Utils
import cats.effect.SyncIO

object PlayerService {
  type PlayerId = String

  private val localStorageKey = "myId"

  def createOrLoadId(): SyncIO[String] = for {
    idOpt <- loadPlayerId()
    id <- idOpt match {
      case Some(id) => SyncIO.pure(id)
      case None     => createAndSaveId()
    }
  } yield (id)

  private def loadPlayerId(): SyncIO[Option[String]] =
    SyncIO(Option(dom.window.localStorage.getItem(localStorageKey)))

  private def createAndSaveId(): SyncIO[String] = for {
    id <- Utils.createId[SyncIO]()
    _ <- SyncIO(dom.window.localStorage.setItem(localStorageKey, id))
  } yield (id)

}
