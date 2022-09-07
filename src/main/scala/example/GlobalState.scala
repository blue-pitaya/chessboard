package example

import example.models.Vec2d
import example.models.Piece

case class GameState(size: Vec2d, pieces: Map[Vec2d, Piece])

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
