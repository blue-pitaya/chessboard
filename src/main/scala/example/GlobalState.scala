package example

import example.models.Vec2d
import example.models.Piece

case class GameState(tiles: Map[Vec2d, Tile], pieces: Map[Vec2d, Piece])

object GlobalState {
  import Settings._

  private def toUiState(gameState: GameState): UiState = UiState(
    tileObjs = Renderer.renderBoard(gameState.tiles, boardDimens),
    pieceObjs = Renderer.renderPieces(gameState.pieces, boardDimens)
  )

  private var _gameState: GameState = GameState(
    Renderer.getTiles(boardDimens.logicSize, tileColorset),
    Game.initPieces
  )

  def gameState: GameState = _gameState

  var uiState: UiState = toUiState(gameState)

  def updateGameState(newGameState: GameState): UiState = {
    _gameState = newGameState
    toUiState(gameState)
  }
}
