package chessboardapi

import cats.effect.IO
import cats.effect.kernel.Ref
import chessboardcore.Model._
import chessboardcore.Model.PlayerState._
import io.circe.generic.auto._
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

      case PlayerReady(playerId, color) =>
        _updateGameState(s => handlePlayerReady(s, playerId, color))

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

  private def handlePlayerReady(
      state: State,
      playerId: String,
      color: PieceColor
  ): State = {
    val nextState = readyPlayer(playerId, color, state)
    if (areBothPlayersReady(nextState.gameState)) startGame(nextState)
    else nextState
  }

  private def startGame(state: State): State = state
    .focus(_.gameState.gameStarted)
    .replace(true)

  private def areBothPlayersReady(gs: GameState): Boolean = gs
    .players
    .filter(_._2.kind != Ready)
    .isEmpty

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
  ): State = state
    .focus(_.gameState.players)
    .modify(pls =>
      pls.get(color) match {
        case None => pls.updated(color, PlayerState(playerId, Sitting))
        case _    => pls
      }
    )

  private def readyPlayer(
      playerId: String,
      color: PieceColor,
      state: State
  ): State = state
    .focus(_.gameState.players)
    .modify(pls =>
      pls.get(color) match {
        case Some(PlayerState(id, Sitting)) if id == playerId =>
          pls.updated(color, PlayerState(id, Ready))
        case _ => pls
      }
    )

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
