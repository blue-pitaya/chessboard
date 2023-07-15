package example

import cats.effect.IO
import chessboardcore.Model
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.implicits._

class HttpClient(client: Client[IO]) {
  private val ApiPath = uri"http://localhost:8080/"

  def fetchGameInfo(id: String): IO[Model.GameInfo] = {
    val request = Request[IO](Method.GET, ApiPath / "game" / id)

    client.expect(request)((jsonOf[IO, Model.GameInfo]))
  }
}
