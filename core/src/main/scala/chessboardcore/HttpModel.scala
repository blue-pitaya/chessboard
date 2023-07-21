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
  case class Move(playerId: String, from: Vec2d, to: Vec2d) extends GameEvent_In

  sealed trait GameEvent_Out
  case class Response(v: TrueGameState, msg: String) extends GameEvent_Out
}
