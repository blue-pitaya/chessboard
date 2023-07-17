package chessboardcore

import cats.syntax.functor._
import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._
import io.circe.syntax._

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

  case class Piece(color: PieceColor, kind: PieceKind)

  case class PlacedPiece(pos: Vec2d, piece: Piece)

  case class TimeSettings(timePerPlayerInSec: Int)
  case class Board(size: Vec2d, pieces: List[Model.PlacedPiece])
  object Board {
    def empty = Board(size = Vec2d(0, 0), pieces = List())
  }

  sealed trait PlayerState
  object PlayerState {
    case object Empty extends PlayerState
    case object Sitting extends PlayerState
    case object Ready extends PlayerState
  }

  case class Players(white: PlayerState, black: PlayerState)
  object Players {
    def init = Players(white = PlayerState.Empty, black = PlayerState.Empty)
  }

  case class GameInfo(
      board: Board,
      timeSettings: TimeSettings,
      players: Players
  )

  // web socket
  sealed trait WsEvent
  case class GetBoard() extends WsEvent
  case class BoardData(v: Board) extends WsEvent
  case class Ok() extends WsEvent

  // can be lifted kurwa
  case class WsEv(e: WsEvent)
}
