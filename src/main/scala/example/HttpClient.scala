package example

import cats.effect.IO
import chessboardcore.Model
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.client.websocket._
import org.http4s.dom._
import org.http4s.implicits._
import cats.effect.Resource

object HttpClient {
  val ApiPath = uri"http://localhost:8080/"
  val WsApiPath = uri"ws://localhost:8080/"

  def gameWebSockerUrl(id: String): String = (WsApiPath / "game" / id / "ws")
    .toString()
}

class HttpClient(client: Client[IO]) {
  import HttpClient._

  def fetchGameInfo(id: String): IO[Model.GameInfo] = {
    val request = Request[IO](Method.GET, ApiPath / "game" / id)

    client.expect(request)((jsonOf[IO, Model.GameInfo]))
  }

  def gameWebSocket(id: String): Resource[IO, WSConnectionHighLevel[IO]] = {
    val request = WSRequest(WsApiPath / "game" / id / "ws")
    WebSocketClient[IO].connectHighLevel(request)
  }

}
