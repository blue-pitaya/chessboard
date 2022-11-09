package example

import example.models.Piece
import xyz.bluepitaya.common.Vec2d

sealed trait GameMove
final case class Move(piece: Piece, from: Vec2d, to: Vec2d) extends GameMove
final case class CastlingMove(kingMove: Move, rookMove: Move) extends GameMove

case class GameState(
    size: Vec2d,
    pieces: Map[Vec2d, Piece],
    moveHistory: Vector[GameMove] = Vector()
) {
  val lastMove: Option[GameMove] = moveHistory.lastOption

  def updatePieces(v: Map[Vec2d, Piece]) = copy(pieces = v)

  def addMoveToHistory(move: GameMove) =
    copy(moveHistory = moveHistory.appended(move))

  def hasPieceMoved(startPos: Vec2d) = moveHistory.exists {
    case Move(piece, from, to) => startPos == from
    case CastlingMove(kingMove, rookMove) => startPos == kingMove.from ||
      startPos == rookMove.from
  }

  def movePiece(move: Move) =
    copy(pieces = pieces.removed(move.from).updated(move.to, move.piece))
}

object GlobalState {
  import Settings._

  private def toUiState(gameState: GameState): UiState = UiState(
    tileObjs = Renderer.renderBoard(
      Renderer.getTiles(gameState.size, tileColorset),
      boardDimens
    ),
    pieceObjs = Renderer.renderPieces(gameState.pieces, boardDimens)
  )

  private var _gameState: GameState =
    GameState(boardDimens.logicSize, Game.initPieces)

  def gameState: GameState = _gameState

  var uiState: UiState = toUiState(gameState)

  def updateGameState(newGameState: GameState): UiState = {
    _gameState = newGameState
    toUiState(gameState)
  }
}
