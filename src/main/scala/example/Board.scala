package example

import com.raquo.laminar.api.L._
import example.components.BoardSettings
import example.components.Tiles
import example.game.CastlingMove
import example.game.GameLogic
import example.game.GameMove
import example.game.GameState
import example.game.Move
import org.scalajs.dom
import xyz.bluepitaya.common.Vec2d
import xyz.bluepitaya.laminardragging.Dragging

import scala.util.Random

object Board {
  val svgElementId = "chessboard"

  def component() = {
    val draggingModule = Dragging.createModule[String]()
    val highlightedTiles = Var(Set[Vec2d]())
    val gameState = Var[GameState](GameState.standardBoard)

    val boardSettings = new BoardSettings {
      override val sizeInPx: Vec2d = Settings.boardDimens.realSizeInPx
      override val colorset: TileColorset = Settings.tileColorset
      override val boardDimens: BoardDimens =
        Settings.boardDimens // FIXME: rewrite to boardSettings
    }

    val piecesComponentSignal = gameState
      .signal
      .map { gs =>
        gs.pieces
          .zipWithIndex
          .map { case ((pos, piece), idx) =>
            Piece.component(
              id = idx.toString(),
              position = pos,
              piece = piece,
              gameState = gs,
              draggingModule = draggingModule,
              highlightTiles = highlightedTiles.set,
              boardSettings = boardSettings,
              updateGameState = gameState.set
            )
          }
          .toList
      }

    val tilesComponent = Tiles
      .component(gameState.signal, highlightedTiles.signal, boardSettings)

    svg.svg(
      svg.idAttr(svgElementId),
      svg.width(Utils.toPx(boardSettings.boardDimens.realSizeInPx.x)),
      svg.height(Utils.toPx(boardSettings.boardDimens.realSizeInPx.y)),
      tilesComponent,
      children <-- piecesComponentSignal,
      draggingModule.documentBindings
    )
  }
}
