package chessboardapi

import cats.effect.IO
import cats.effect.kernel.Ref
import chessboardcore.Model.PlayerState.Empty
import chessboardcore.Model._
import io.circe.generic.auto._
import monocle.AppliedLens
import monocle.syntax.all._
import org.http4s.Response
import org.http4s.server.websocket.WebSocketBuilder2

object TrueGameService {
  case class State(gameState: GameState)
  // TODO: this in unnecessary
  case class Module(subsrice: WebSocketBuilder2[IO] => IO[Response[IO]])

  private def createState(board: Board): IO[Ref[IO, State]] = Ref
    .of[IO, State](State(GameState.empty.copy(board = board)))

  private def handle(e: WsEv, stateRef: Ref[IO, State]): IO[WsEv] = {
    val _updateGameState = (f: State => State) => updateGameState(stateRef, f)

    e.e match {
      case GetGameState() => for {
          state <- stateRef.get
        } yield (WsEv(GameStateData(state.gameState)))

      case PlayerSit(playerId, color) =>
        _updateGameState(s => sitPlayer(color, playerId, s))

      case PlayerReady(playerId) =>
        _updateGameState(s => handlePlayerReady(s, playerId))

      case _ => IO.pure(WsEv(Ok()))
    }
  }

  private def handlePlayerReady(state: State, playerId: String): State = {
    val nextState = readyPlayer(playerId, state)
    if (areBothPlayersReady(nextState.gameState)) startGame(nextState)
    else nextState
  }

  private def startGame(state: State): State = state
    .focus(_.gameState.gameStarted)
    .replace(true)

  private def areBothPlayersReady(gs: GameState): Boolean =
    (gs.whitePlayerState, gs.blackPlayerState) match {
      case (PlayerState.Ready(_), PlayerState.Ready(_)) => true
      case _                                            => false
    }

  private def updateGameState(
      stateRef: Ref[IO, State],
      f: State => State
  ): IO[WsEv] = for {
    nextState <- stateRef.updateAndGet(f)
  } yield (WsEv(GameStateData(nextState.gameState)))

  private def sitPlayer(
      color: PieceColor,
      playerId: String,
      state: State
  ): State = {
    val playerLens = playerByColor(color, state)

    playerLens.get match {
      case Empty => playerLens.replace(PlayerState.Sitting(playerId))
      case _     => state
    }
  }

  private def readyPlayer(playerId: String, state: State): State = {
    import PlayerState._

    (state.gameState.whitePlayerState, state.gameState.blackPlayerState) match {
      case (Sitting(id), _) if id == playerId =>
        state.focus(_.gameState.whitePlayerState).replace(Ready(id))
      case (_, Sitting(id)) if id == playerId =>
        state.focus(_.gameState.blackPlayerState).replace(Ready(id))
      case _ => state
    }
  }

  private def playerByColor(
      color: PieceColor,
      state: State
  ): AppliedLens[State, PlayerState] = {
    color match {
      case Black => state.focus(_.gameState.blackPlayerState)
      case White => state.focus(_.gameState.whitePlayerState)
    }
  }

  def create(board: Board): IO[Module] = {
    for {
      stateRef <- createState(board)
      _handle = (e: WsEv) => handle(e, stateRef)
      x <- WebSockerBroadcaster.create(_handle)
      s = (ws: WebSocketBuilder2[IO]) => {
        ws.build(x._1, x._2)
      }
    } yield (Module(s))
  }
}
