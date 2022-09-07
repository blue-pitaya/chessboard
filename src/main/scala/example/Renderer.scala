package example

import example.models.Vec2d
import example.models.Vec2d._
import example.models.HexColor
import scala.scalajs.js.annotation._
import scala.scalajs.js.JSConverters._
import scala.scalajs.js
import example.models.Piece

case class Tile(color: HexColor)

case class TileColorset(dark: HexColor, light: HexColor)

case class BoardDimens(logicSize: Vec2d, realSizeInPx: Vec2d)

@JSExportAll
case class TileObj(
    id: String,
    position: Vec2d,
    size: Vec2d,
    color: String,
    fileMark: js.UndefOr[String],
    rankMark: js.UndefOr[String]
) extends DrawingObj

@JSExportAll
case class PieceObj(
    id: String,
    gamePosition: Vec2d,
    basePosition: Vec2d,
    draggingPosition: Vec2d,
    size: Vec2d,
    pieceColor: String,
    pieceKind: String
) extends Draggable {
  def position: Vec2d = basePosition + draggingPosition
}

object Renderer {
  private def getTileSize(boardDimens: BoardDimens): Vec2d = Vec2d(
    boardDimens.realSizeInPx.x / boardDimens.logicSize.x,
    boardDimens.realSizeInPx.y / boardDimens.logicSize.y
  )

  private def toRealPos(logicPos: Vec2d, boardDimens: BoardDimens): Vec2d = {
    val tileSize = getTileSize(boardDimens)
    val xInPx = logicPos.x * tileSize.x
    // note: board positions and render positions has opposite y axis
    val yInPx = boardDimens.realSizeInPx.y - tileSize.y -
      (logicPos.y * tileSize.y)

    Vec2d(xInPx, yInPx)
  }

  def getTiles(size: Vec2d, colorset: TileColorset): Map[Vec2d, Tile] = Vec2d
    .zero
    .matrixUntil(size)
    .map { p =>
      val isDark = (p.x + p.y) % 2 == 0
      val tile = Tile(color = if (isDark) colorset.dark else colorset.light)
      (p -> tile)
    }
    .toMap

  def toLogicPostion(pos: Vec2d, boardDimens: BoardDimens): Vec2d = Vec2d(
    pos.x / getTileSize(boardDimens).x,
    (boardDimens.realSizeInPx.y - pos.y) / getTileSize(boardDimens).y
  )

  // TODO: change name
  def renderBoard(
      tiles: Map[Vec2d, Tile],
      boardDimens: BoardDimens
  ): List[TileObj] = {
    def fileMark(position: Vec2d): Option[String] = position match {
      case Vec2d(x, 0) => Some(('a' + x).toChar.toString())
      case _           => None
    }

    def rankMark(position: Vec2d): Option[String] = position match {
      case Vec2d(0, y) => Some((1 + y).toString())
      case _           => None
    }

    tiles
      .map { case (pos, tile) =>
        TileObj(
          id = IdGenerator.nextId,
          position = toRealPos(pos, boardDimens),
          size = getTileSize(boardDimens),
          color = tile.color.value,
          fileMark = fileMark(pos).orUndefined,
          rankMark = rankMark(pos).orUndefined
        )
      }
      .toList
  }

  def renderPieces(
      pieces: Map[Vec2d, Piece],
      boardDimens: BoardDimens
  ): List[PieceObj] = {
    pieces
      .map { case (pos, piece) =>
        PieceObj(
          id = IdGenerator.nextId,
          gamePosition = pos,
          basePosition = toRealPos(pos, boardDimens),
          draggingPosition = Vec2d.zero,
          size = getTileSize(boardDimens),
          pieceColor = piece.color.toString(),
          pieceKind = piece.kind.toString()
        )
      }
      .toList
  }
}
