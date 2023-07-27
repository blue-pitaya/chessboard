package chessboardapi.game

import cats.effect.IO
import cats.effect.kernel.Ref
import cats.effect.std.Queue
import chessboardcore.HttpModel
import chessboardcore.HttpModel.GetGameState
import chessboardcore.HttpModel.Move
import chessboardcore.HttpModel.PlayerReady
import chessboardcore.HttpModel.PlayerSit
import chessboardcore.Model.PlayerState._
import chessboardcore.Model._
import chessboardcore.Vec2d
import chessboardcore.gamelogic.GameLogic
import fs2._
import fs2.concurrent.Topic
import monocle.syntax.all._
import org.http4s.Response
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame

object GameService {
  sealed trait Recipient
  object Recipient {
    case class User(token: String) extends Recipient
    case class Everybody() extends Recipient
  }

  case class OutputMsg(recipient: Recipient, event: HttpModel.GameEvent_Out)

  case class State(
      gameState: TrueGameState,
      players: Map[PieceColor, PlayerState],
      gameStarted: Boolean
  )
  object State {
    def init(board: Board) = State(
      gameState = GameLogic.createGame(board),
      players = Map(),
      gameStarted = false
    )
  }

  case class Module(join: WebSocketBuilder2[IO] => IO[Response[IO]])

  def create(board: Board): IO[Module] = {
    for {
      stateRef <- Ref.of[IO, State](State.init(board))
      topic <- Topic[IO, OutputMsg]
      queue <- Queue.unbounded[IO, HttpModel.WsInputMessage]
      // TODO: fiber should be stopped later
      _ <- Stream
        .repeatEval(queue.take)
        .evalMap(msg => stateRef.modify(s => handle(msg, s)))
        .flatMap(Stream.emits)
        .through(topic.publish)
        .compile
        .drain
        .start
      _ <- IO.println("Game started.")
      joinFn =
        (wsBuilder: WebSocketBuilder2[IO]) => join(wsBuilder, topic, queue)
    } yield (Module(joinFn))
  }

  private def join(
      wsBuilder: WebSocketBuilder2[IO],
      topic: Topic[IO, OutputMsg],
      queue: Queue[IO, HttpModel.WsInputMessage]
  ): IO[Response[IO]] = for {
    tokenRef <- Ref.of[IO, Option[String]](None)
    resp <- {
      val in: fs2.Pipe[IO, WebSocketFrame, Unit] = stream =>
        stream
          .evalMap(frame => JsonHelpers.decodeFrame(frame))
          .evalTap(inputMsg => tokenRef.set(Some(inputMsg.token)))
          .foreach(queue.offer)
      val out: Stream[IO, WebSocketFrame] = topic
        .subscribeUnbounded
        .evalFilter(outputMsg =>
          for {
            tokenOpt <- tokenRef.get
            result = (tokenOpt, outputMsg) match {
              case (_, OutputMsg(Recipient.Everybody(), _)) => true
              case (Some(token), OutputMsg(Recipient.User(forToken), _)) =>
                token == forToken
              case _ => false
            }
          } yield (result)
        )
        .evalMap(outputMsg => JsonHelpers.encodeValue(outputMsg.event))

      wsBuilder.build(out, in)
    }
  } yield (resp)

  private def handle(
      msg: HttpModel.WsInputMessage,
      state: State
  ): (State, Seq[OutputMsg]) = msg.event match {
    case GetGameState() =>
      (state, Seq(OutputMsg(Recipient.User(msg.token), toResponse(state))))
    case Move(playerId, from, to, playerColor) =>
      val result = handleMovePlayer(playerId, from, to, playerColor, state)
      result match {
        case Left(err) => (
            state,
            Seq(
              OutputMsg(Recipient.User(msg.token), toResponse(state, err.msg))
            )
          )
        case Right(nextState) => (
            nextState,
            Seq(OutputMsg(Recipient.Everybody(), toResponse(nextState)))
          )
      }
    case PlayerReady(playerId, color) =>
      val nextState = handlePlayerReady(state, playerId, color)
      (nextState, Seq(OutputMsg(Recipient.Everybody(), toResponse(nextState))))
    case PlayerSit(playerId, color) =>
      val nextState = handleSitPlayer(color, playerId, state)
      (nextState, Seq(OutputMsg(Recipient.Everybody(), toResponse(nextState))))
  }

  private def handleMovePlayer(
      playerId: String,
      from: Vec2d,
      to: Vec2d,
      color: PieceColor,
      state: State
  ): Either[GameModel.MakeMoveFail, State] = {
    val verifyPlayerColor: Either[GameModel.MakeMoveFail, Unit] = {
      if (
        state
          .players
          .exists { case (c, PlayerState(id, _)) =>
            c == color && id == playerId
          }
      ) Right(())
      else Left(GameModel.MakeMoveFail("It's not your piece!"))
    }

    for {
      _ <- verifyPlayerColor
      nextGameState <-
        GameLogic.makeMove(from, to, color, state.gameState) match {
          case Left(errMsg) => Left(GameModel.MakeMoveFail(errMsg))
          case Right(v)     => Right(v)
        }
      nextState = state.focus(_.gameState).replace(nextGameState)
    } yield (nextState)
  }

  private def toResponse(s: State): HttpModel.GameEvent_Out = HttpModel
    .GameEvent_Out(
      gameState = s.gameState,
      msg = None,
      gameStarted = s.gameStarted,
      players = s.players
    )

  private def toResponse(s: State, msg: String): HttpModel.GameEvent_Out =
    toResponse(s).copy(msg = Some(msg))

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
}
