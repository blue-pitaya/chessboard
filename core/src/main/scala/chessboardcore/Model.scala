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
  object PieceColor {
    def opposite(color: PieceColor): PieceColor = color match {
      case White => Black
      case Black => White
    }
  }

  case class Piece(color: PieceColor, kind: PieceKind)

  case class PlacedPiece(pos: Vec2d, piece: Piece)

  case class Board(size: Vec2d, pieces: List[Model.PlacedPiece])
  object Board {
    def empty = Board(size = Vec2d(0, 0), pieces = List())
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
      whitePlayerState: Option[PlayerState],
      blackPlayerState: Option[PlayerState],
      gameStarted: Boolean,
      turn: PieceColor,
      gameOver: Option[GameOverState]
  )
  object GameState {
    def empty = GameState(Board.empty, None, None, false, White, None)
  }

  // web socket
  sealed trait WsEvent
  case class GetGameState() extends WsEvent
  case class GameStateData(v: GameState) extends WsEvent
  case class PlayerSit(playerId: String, color: PieceColor) extends WsEvent
  case class PlayerReady(playerId: String) extends WsEvent
  case class Move(playerId: String, from: Vec2d, to: Vec2d) extends WsEvent
  case class Ok() extends WsEvent

  // TODO: can be lifted kurwa
  case class WsEv(e: WsEvent)
}
