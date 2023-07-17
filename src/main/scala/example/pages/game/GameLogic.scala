package example.pages.game

import chessboardcore.Model
import chessboardcore.Model._
import com.raquo.laminar.api.L._
import io.laminext.websocket.WebSocket

object GameLogic {
  case class Module(sendWsEventObserver: Observer[Model.WsEv])

  def wireGamePage2(
      events: EventStream[GamePage2.Event],
      state: GamePage2.State,
      ws: WebSocket[WsEv, WsEv]
  )(implicit owner: Owner): Unit = {
    val _handleWsEvent = (e: WsEv) => handleWsEvent(e, state)
    val _handleEvent = (e: GamePage2.Event) => handleEvent(e, state, ws.sendOne)
    val eventObserver = Observer[GamePage2.Event](_handleEvent)

    ws.received.addObserver(Observer[Model.WsEv](_handleWsEvent))
    events.addObserver(eventObserver)
  }

  def handleWsEvent(e: WsEv, state: GamePage2.State): Unit = e match {
    case WsEv(BoardData(v)) => state.board.set(v)
    case _                  => ()
  }

  def handleEvent(
      e: GamePage2.Event,
      state: GamePage2.State,
      sendWsEvent: WsEv => Unit
  ): Unit = e match {
    case GamePage2.PingClicked() => println(";)")
    case GamePage2.LoadBoard()   => sendWsEvent(WsEv(Model.GetBoard()))
  }
}
