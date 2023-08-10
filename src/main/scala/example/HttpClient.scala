package example

import cats.effect.IO
import chessboardcore.HttpModel
import chessboardcore.Model
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe._
import org.http4s.client.Client
import macros.Envs

object HttpClient {
  val ApiPath = Uri.fromString(Envs.ApiPath).toOption.get
  val WsApiPath = Uri.fromString(Envs.WsApiPath).toOption.get

  def gameWebSockerUrl(id: String): String = (WsApiPath / "game" / id / "ws")
    .toString()
}

class HttpClient(client: Client[IO]) {
  import HttpClient._

  def createGame(board: Model.Board): IO[HttpModel.CreateGame_Out] = {
    // json implicits
    import chessboardcore.Model._

    val uri = ApiPath / "game"
    val data = HttpModel.CreateGame_In(board)
    val request = Request[IO](Method.PUT, uri).withEntity(data.asJson)

    client.expect[HttpModel.CreateGame_Out](request)
  }

}
