package chessboardcore

object Model {
  sealed trait Fig
  case object Pawn extends Fig
  case object Rook extends Fig
  case object Knight extends Fig
  case object Bishop extends Fig
  case object Queen extends Fig
  case object King extends Fig

  sealed trait FigColor
  case object White extends FigColor
  case object Black extends FigColor

  case class Piece(color: FigColor, kind: Fig)

  case class PlacedPiece(pos: Vec2d, piece: Piece)

  case class TimeSettings(timePerPlayerInSec: Int)
  case class Board(size: Vec2d, pieces: List[Model.PlacedPiece])

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
}
