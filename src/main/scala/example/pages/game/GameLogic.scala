package example.pages.game

import chessboardcore.Model
import chessboardcore.Model._
import com.raquo.laminar.api.L._
import io.laminext.websocket.WebSocket

object GameLogic {
  case class Module(sendWsEventObserver: Observer[Model.WsEv])

  def wireGamePage2(
      events: EventStream[GamePage2.Event],
      plSectionEvents: EventStream[PlayersSection.Event],
      state: GamePage2.State,
      ws: WebSocket[WsEv, WsEv]
  )(implicit owner: Owner): Unit = {
    ws.received.addObserver(Observer[Model.WsEv](e => handleWsEvent(e, state)))
    events.addObserver(
      Observer[GamePage2.Event](e => handleEvent(e, state, ws.sendOne))
    )
    plSectionEvents.addObserver(
      Observer[PlayersSection.Event](e =>
        handlePlSectionEvent(e, state, ws.sendOne)
      )
    )
  }

  def handleWsEvent(e: WsEv, state: GamePage2.State): Unit = e match {
    case WsEv(GameStateData(v)) => state.gameState.set(v)
    case _                      => ()
  }

  def handleEvent(
      e: GamePage2.Event,
      state: GamePage2.State,
      sendWsEvent: WsEv => Unit
  ): Unit = e match {
    case GamePage2.RequestGameState() => sendWsEvent(WsEv(GetGameState()))
  }

  def handlePlSectionEvent(
      e: PlayersSection.Event,
      state: GamePage2.State,
      sendWsEvent: WsEv => Unit
  ): Unit = e match {
    case PlayersSection.PlayerSit(color) => sendWsEvent(WsEv(PlayerSit(color)))
  }
}
