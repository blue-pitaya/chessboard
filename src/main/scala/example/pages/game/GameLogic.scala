package example.pages.game

import chessboardcore.Model
import com.raquo.laminar.api.L._
import io.laminext.websocket.WebSocket

object GameModel {
  case class State()

}

object GameLogic {
  case class Module(sendWsEventObserver: Observer[Model.WsEvent])

  def create(
      ws: WebSocket[Model.WsEvent, Model.WsEvent]
  )(implicit owner: Owner): Module = {
    ws.send.onNext(Model.MPing("ok"))

    ws.received.addObserver(Observer[Model.WsEvent](v => println(v)))

    Module(sendWsEventObserver = ws.send)
  }

  def wireGamePage2(module: Module, events: EventStream[GamePage2.Event])(
      implicit owner: Owner
  ): Unit = {
    val _handleEvent = (e: GamePage2.Event) => handleEvent(e, module)
    val eventObserver = Observer[GamePage2.Event](_handleEvent)
    events.addObserver(eventObserver)
  }

  def handleEvent(e: GamePage2.Event, module: Module): Unit = e match {
    case GamePage2.PingClicked() =>
      module.sendWsEventObserver.onNext(Model.MPing("hey"))
  }
}
