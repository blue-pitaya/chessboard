package chessboardcore

import io.circe.KeyEncoder
import io.circe.KeyDecoder

object Model {
  sealed trait PieceKind
  case object Pawn extends PieceKind
  case object Rook extends PieceKind
  case object Knight extends PieceKind
  case object Bishop extends PieceKind
  case object Queen extends PieceKind
  case object King extends PieceKind

  sealed trait PieceColor
  case object White extends PieceColor
  case object Black extends PieceColor
  object PieceColor {
    def opposite(color: PieceColor): PieceColor = color match {
      case White => Black
      case Black => White
    }
  }

  case class Piece(color: PieceColor, kind: PieceKind)

  case class PlacedPiece(pos: Vec2d, piece: Piece)

  case class Board(size: Vec2d, pieces: Map[Vec2d, Piece])
  object Board {
    def empty = Board(size = Vec2d(0, 0), pieces = Map())
  }

  // FIXME
  implicit val vec2dKeyEncoder = new KeyEncoder[Vec2d] {
    override def apply(key: Vec2d): String = ???
  }
  implicit val vec2dKeyDecoder = new KeyDecoder[Vec2d] {
    override def apply(key: String): Option[Vec2d] = ???
  }

  case class PlayerState(id: String, kind: PlayerState.Kind)
  object PlayerState {
    sealed trait Kind
    case object Sitting extends Kind
    case object Ready extends Kind
  }

  sealed trait GameOverState
  object GameOverState {
    case class WinFor(color: PieceColor, reason: String) extends GameOverState
    case class Draw(reason: String) extends GameOverState
  }

  case class GameState(
      board: Board,
      players: Map[PieceColor, PlayerState],
      gameStarted: Boolean,
      turn: PieceColor,
      gameOver: Option[GameOverState]
  )
  object GameState {
    def empty = GameState(Board.empty, Map(), false, White, None)
  }

  implicit val pieceColorKeyEncoder = new KeyEncoder[PieceColor] {
    override def apply(key: PieceColor): String = key match {
      case White => White.toString()
      case Black => Black.toString()
    }
  }
  implicit val pieceColorKeyDecoder = new KeyDecoder[PieceColor] {
    override def apply(key: String): Option[PieceColor] = key match {
      case v if v == White.toString() => Some(White)
      case v if v == Black.toString() => Some(Black)
      case _                          => None
    }
  }

  // web socket
  // TODO: should go to httpModel
  sealed trait WsEvent
  case class GetGameState() extends WsEvent
  case class GameStateData(v: GameState) extends WsEvent
  case class PlayerSit(playerId: String, color: PieceColor) extends WsEvent
  // FIXME: add color!
  case class PlayerReady(playerId: String, color: PieceColor) extends WsEvent
  case class Move(playerId: String, from: Vec2d, to: Vec2d) extends WsEvent
  case class Ok() extends WsEvent

  // TODO: can be lifted kurwa
  case class WsEv(e: WsEvent)
}
