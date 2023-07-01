package example.pages.creator

import com.raquo.laminar.api.L._
import example.game.Vec2d

object BoardContainer {
  case class State(boardLogicSize: Vec2d, boardRealSize: Vec2d)
  object State {
    def empty = State(boardLogicSize = Vec2d.zero, boardRealSize = Vec2d.zero)
  }

  case class TileState(position: Vec2d, size: Int, hexColor: String)

  def component(state: State): SvgElement = {
    val tileStates = createTiles(state)

    svg.svg(
      svg.cls("min-w-[800px] h-[800px] bg-stone-800"),
      svg.g(tileStates.map(renderTile))
    )
  }

  def renderTile(state: TileState): SvgElement = svg.rect(
    svg.x(state.position.x.toString()),
    svg.y(state.position.y.toString()),
    svg.width(state.size.toString()),
    svg.height(state.size.toString()),
    svg.fill(state.hexColor)
  )

  def createTiles(state: State): List[TileState] = tileLogicPositions(
    state.boardLogicSize
  ).map(pos =>
    TileState(
      position = getTilePos(state, pos),
      size = getTileSize(state),
      hexColor = tileHexColor(pos)
    )
  )

  def tileLogicPositions(boardLogicSize: Vec2d): List[Vec2d] =
    (0.until(boardLogicSize.x))
      .map(x => (0.until(boardLogicSize.y)).map(y => Vec2d(x, y)))
      .flatten
      .toList

  def getTilePos(boardState: State, tileLogicPos: Vec2d): Vec2d = {
    val tileSize = getTileSize(boardState)
    val totalTilesWidth = boardState.boardLogicSize.x * tileSize
    val totalTilesHeight = boardState.boardLogicSize.y * tileSize
    val offsetX = (boardState.boardRealSize.x - totalTilesWidth) / 2
    val offsetY = (boardState.boardRealSize.y - totalTilesHeight) / 2
    val x = tileLogicPos.x * tileSize
    val y =
      (boardState.boardRealSize.y - tileSize) - (tileLogicPos.y * tileSize)

    Vec2d(offsetX + x, y - offsetY)
  }

  def getTileSize(boardState: State): Int = {
    val maxSize = 100
    val x = boardState.boardRealSize.x / boardState.boardLogicSize.x
    val y = boardState.boardRealSize.y / boardState.boardLogicSize.y

    Math.min(Math.min(maxSize, x), Math.min(maxSize, y))
  }

  def tileHexColor(logicPos: Vec2d): String = {
    val blackTileColor = "#b58863"
    val whiteTileColor = "#f0d9b5"

    if ((logicPos.x + logicPos.y) % 2 == 0) blackTileColor
    else whiteTileColor
  }
}
