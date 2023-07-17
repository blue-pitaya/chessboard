package example.pages.game

import chessboardcore.Model
import com.raquo.laminar.api.L._
import example.HttpClient
import io.circe.generic.auto._
import io.laminext.websocket.WebSocket
import io.laminext.websocket.circe._

object GamePage2 {
  sealed trait Event
  case class PingClicked() extends Event

  def component(): Element = {
    val bus = new EventBus[Event]
    val url = HttpClient.gameWebSockerUrl("abc")
    val ws: WebSocket[Model.WsEvent, Model.WsEvent] = WebSocket
      .url(url)
      .json[Model.WsEvent, Model.WsEvent]
      .build()

    div(
      button("Ping", onClick.mapTo(PingClicked()) --> bus),
      ws.connect,
      onMountCallback(ctx => onMounted(bus, ctx.owner, ws))
    )
  }

  private def onMounted(
      bus: EventBus[Event],
      owner: Owner,
      ws: WebSocket[Model.WsEvent, Model.WsEvent]
  ): Unit = {
    val module = GameLogic.create(ws)(owner)
    GameLogic.wireGamePage2(module, bus.events)(owner)
  }
}
