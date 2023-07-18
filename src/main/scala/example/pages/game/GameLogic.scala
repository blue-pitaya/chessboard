package example.pages.game

import chessboardcore.Model
import chessboardcore.Model._
import com.raquo.laminar.api.L._
import io.laminext.websocket.WebSocket
import example.pages.game.PlayersSection.PlayerReady

object GameLogic {
  case class Module(sendWsEventObserver: Observer[Model.WsEv])

  def wireGamePage2(
      events: EventStream[GamePage.Event],
      plSectionEvents: EventStream[PlayersSection.Event],
      state: GamePage.State,
      ws: WebSocket[WsEv, WsEv]
  )(implicit owner: Owner): Unit = {
    ws.received.addObserver(Observer[Model.WsEv](e => handleWsEvent(e, state)))
    events.addObserver(
      Observer[GamePage.Event](e => handleEvent(e, state, ws.sendOne))
    )
    plSectionEvents.addObserver(
      Observer[PlayersSection.Event](e =>
        handlePlSectionEvent(e, state, ws.sendOne)
      )
    )
  }

  def handleWsEvent(e: WsEv, state: GamePage.State): Unit = e match {
    case WsEv(GameStateData(v)) => state.gameState.set(v)
    case _                      => ()
  }

  def handleEvent(
      e: GamePage.Event,
      state: GamePage.State,
      sendWsEvent: WsEv => Unit
  ): Unit = e match {
    case GamePage.RequestGameState() => sendWsEvent(WsEv(GetGameState()))
  }

  def handlePlSectionEvent(
      e: PlayersSection.Event,
      state: GamePage.State,
      sendWsEvent: WsEv => Unit
  ): Unit = e match {
    case PlayersSection.PlayerSit(color) =>
      sendWsEvent(WsEv(PlayerSit(state.playerId, color)))
    case PlayerReady() => sendWsEvent(WsEv(Model.PlayerReady(state.playerId)))
  }
}
