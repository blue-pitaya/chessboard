package chessboardapi

import cats.effect.IO
import cats.effect.kernel.Ref
import chessboardcore.HttpModel._
import chessboardcore.Model.PlayerState._
import chessboardcore.Model._
import chessboardcore.gamelogic.GameLogic
import fs2.Stream
import io.circe.generic.auto._
import monocle.syntax.all._
import org.http4s.Response
import org.http4s.server.websocket.WebSocketBuilder2

object TrueGameService {
  case class State(
      gameState: TrueGameState,
      players: Map[PieceColor, PlayerState],
      gameStarted: Boolean
  )
  // TODO: this in unnecessary
  case class Module(subsrice: WebSocketBuilder2[IO] => IO[Response[IO]])

  def create(board: Board): IO[Module] = {
    for {
      stateRef <- createState(board)
      _handle = (e: GameEvent_In) => handle(e, stateRef)
      x <- WebSockerBroadcaster.create(_handle)
      s = (ws: WebSocketBuilder2[IO]) => {
        ws.build(x._1, x._2)
      }
    } yield (Module(s))
  }

  private def createState(board: Board): IO[Ref[IO, State]] = Ref.of[IO, State](
    State(
      gameState = GameLogic.createGame(board),
      players = Map(),
      gameStarted = false
    )
  )

  private def toResponse(s: State): GameEvent_Out = GameEvent_Out(
    gameState = s.gameState,
    msg = None,
    gameStarted = s.gameStarted,
    players = s.players
  )

  private def toRespons(s: State, msg: String): GameEvent_Out = toResponse(s)
    .copy(msg = Some(msg))

  private def handle(
      e: GameEvent_In,
      stateRef: Ref[IO, State]
  ): fs2.Stream[IO, GameEvent_Out] = e match {
    case GetGameState() => for {
        state <- Stream.eval(stateRef.get)
        resp = toResponse(state)
      } yield (resp)

    case PlayerSit(playerId, color) => for {
        nextState <- Stream
          .eval(stateRef.updateAndGet(s => handleSitPlayer(color, playerId, s)))
        resp = toResponse(nextState)
      } yield (resp)

    case PlayerReady(playerId, color) => for {
        nextState <- Stream.eval(
          stateRef.updateAndGet(s => handlePlayerReady(s, playerId, color))
        )
        resp = toResponse(nextState)
      } yield (resp)

    case Move(playerId, from, to, color) =>
      val verifyPlayerColor = (s: State) => {
        if (
          s.players
            .exists { case (c, PlayerState(id, _)) =>
              c == color && id == playerId
            }
        ) IO.unit
        else IO
          .raiseError(GameServiceModel.MakeMoveFail("It's not your piece!"))
      }
      val makeMove = for {
        state <- stateRef.get
        _ <- verifyPlayerColor(state)
        nextGameState <-
          GameLogic.makeMove(from, to, color, state.gameState) match {
            case Left(errMsg) =>
              IO.raiseError(GameServiceModel.MakeMoveFail(errMsg))
            case Right(v) => IO.pure(v)
          }
        nextState <-
          stateRef.updateAndGet(_.focus(_.gameState).replace(nextGameState))
      } yield (toResponse(nextState))

      Stream
        .eval(makeMove)
        .handleErrorWith { case GameServiceModel.MakeMoveFail(msg) =>
          val resp = for {
            state <- stateRef.get
            resp = toRespons(state, msg)
          } yield (resp)
          Stream.eval(resp)
        }
  }

  private def handleSitPlayer(
      color: PieceColor,
      playerId: String,
      state: State
  ): State = state
    .focus(_.players)
    .modify(pls =>
      pls.get(color) match {
        case None => pls.updated(color, PlayerState(playerId, Sitting))
        case _    => pls
      }
    )

  private def handlePlayerReady(
      state: State,
      playerId: String,
      color: PieceColor
  ): State = {
    val setPlayerReady = (s: State) =>
      s.focus(_.players)
        .modify(pls =>
          pls.get(color) match {
            case Some(PlayerState(id, Sitting)) if id == playerId =>
              pls.updated(color, PlayerState(id, Ready))
            case _ => pls
          }
        )
    val areBothPlayersReady = (s: State) =>
      (s.players.get(White), s.players.get(Black)) match {
        case (Some(PlayerState(_, Ready)), Some(PlayerState(_, Ready))) => true
        case _                                                          => false
      }
    val startGameIfBothPlayersReady = (s: State) =>
      if (areBothPlayersReady(s)) s.focus(_.gameStarted).replace(true)
      else s

    (setPlayerReady andThen startGameIfBothPlayersReady)(state)
  }
}
