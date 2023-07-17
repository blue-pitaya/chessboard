package chessboardcore

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

  case class Board(size: Vec2d, pieces: List[Model.PlacedPiece])
  object Board {
    def empty = Board(size = Vec2d(0, 0), pieces = List())
  }

  sealed trait PlayerState
  object PlayerState {
    case object Empty extends PlayerState
    case object Sitting extends PlayerState
    case object Ready extends PlayerState

    def default = Empty
  }

  case class GameState(
      board: Board,
      whitePlayerState: PlayerState,
      blackPlayerState: PlayerState
  )
  object GameState {
    def empty = GameState(Board.empty, PlayerState.default, PlayerState.default)
  }

  // web socket
  sealed trait WsEvent
  case class GetGameState() extends WsEvent
  case class GameStateData(v: GameState) extends WsEvent
  case class PlayerSit(color: PieceColor) extends WsEvent
  case class Ok() extends WsEvent

  // TODO: can be lifted kurwa
  case class WsEv(e: WsEvent)
}
