package chessboardcore

import Model._

object HttpModel {
  case class CreateChessboard_In(boardSize: Vec2d, pieces: List[PlacedPiece])

  case class CreateGame_In(board: Board)
  case class CreateGame_Out(id: String)

  // websockets
  sealed trait GameEvent_In
  case class GetGameState() extends GameEvent_In
  case class PlayerSit(playerId: String, color: PieceColor) extends GameEvent_In
  case class PlayerReady(playerId: String, color: PieceColor)
      extends GameEvent_In
  // player color is needed, since player can play with himself (thus having same id on white and black)
  case class Move(
      playerId: String,
      from: Vec2d,
      to: Vec2d,
      playerColor: PieceColor
  ) extends GameEvent_In

  case class GameEvent_Out(
      gameState: TrueGameState,
      msg: Option[String],
      gameStarted: Boolean,
      players: Map[PieceColor, PlayerState]
  )
}
