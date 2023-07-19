package chessboardapi

import cats.effect.IO
import cats.effect.kernel.Ref
import chessboardcore.Model._
import chessboardcore.Model.PlayerState._
import io.circe.generic.auto._
import monocle.AppliedLens
import monocle.syntax.all._
import org.http4s.Response
import org.http4s.server.websocket.WebSocketBuilder2
import chessboardcore.Vec2d
import chessboardcore.gamelogic.MoveLogic

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

      case Move(playerId, from, to) =>
        _updateGameState(s => tryMakeMove(s, playerId, from, to))

      case _ => IO.pure(WsEv(Ok()))
    }
  }

  private def tryMakeMove(
      state: State,
      playerId: String,
      from: Vec2d,
      to: Vec2d
  ): State = {
    if (MoveLogic.canMove(state.gameState.board, from, to))
      makeMove(state, from, to)
    else state
  }

  private def makeMove(state: State, from: Vec2d, to: Vec2d): State = {
    lazy val lens = state.focus(_.gameState.board.pieces)

    state.gameState.board.pieces.find(p => p.pos == from) match {
      case None => state
      case Some(placedPiece) =>
        val nextPlacedPiece = PlacedPiece(to, placedPiece.piece)
        lens.modify(_.filter(p => p.pos != from).appended(nextPlacedPiece))
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
      case (Some(PlayerState(_, Ready)), Some(PlayerState(_, Ready))) => true
      case _                                                          => false
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
      case None => playerLens.replace(Some(PlayerState(playerId, Sitting)))
      case _    => state
    }
  }

  private def readyPlayer(playerId: String, state: State): State = {
    import PlayerState._

    (state.gameState.whitePlayerState, state.gameState.blackPlayerState) match {
      case (Some(PlayerState(id, Sitting)), _) if id == playerId =>
        state
          .focus(_.gameState.whitePlayerState)
          .replace(Some(PlayerState(id, Ready)))
      case (_, Some(PlayerState(id, Sitting))) if id == playerId =>
        state
          .focus(_.gameState.blackPlayerState)
          .replace(Some(PlayerState(id, Ready)))
      case _ => state
    }
  }

  private def playerByColor(
      color: PieceColor,
      state: State
  ): AppliedLens[State, Option[PlayerState]] = {
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
