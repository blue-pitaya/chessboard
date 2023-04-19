package example

import com.raquo.laminar.api.L._
import example.components.BoardSettings
import example.components.Tiles
import example.game.GameMove
import example.game.GameState
import org.scalajs.dom
import xyz.bluepitaya.common.Vec2d
import xyz.bluepitaya.laminardragging.Dragging

import scala.util.Random
import example.game.CastlingMove
import example.game.Move
import example.game.GameLogic

//FIXME: remove split pieces, add Pieces.component (like tiles)

object Board {
  val svgElementId = "chessboard"

  case class State(
      boardDimens: BoardDimens,
      tileColorset: TileColorset,
      draggingModule: Dragging.DraggingModule[String],
      gameMoveBus: EventBus[GameMove],
      highlightedTiles: Var[Set[Vec2d]],
      gameState: Var[GameState]
  )

  def component() = {
    val state = Board.State(
      boardDimens = Settings.boardDimens, // FIXME:
      tileColorset = Settings.tileColorset,
      draggingModule = Dragging.createModule[String](),
      gameMoveBus = new EventBus[GameMove],
      highlightedTiles = Var(Set()),
      gameState = Var[GameState](GameState.standardBoard)
    )

    // FIXME: split on position is not correct
    def pieceComponentsSignal(container: dom.Element) = state
      .gameState
      .signal
      .map(_.pieces.toVector)
      .split(_._1) { case (pos, (_, piece), pieceSignal) =>
        val id = Random.alphanumeric.take(16).toList.toString() // FIXME:
        Piece.component(
          id = id,
          position = pos,
          piece = piece,
          boardState = state,
          container = container
        )
      }

    val tilesComponent = Tiles.component(
      state.gameState.signal,
      state.highlightedTiles.signal,
      new BoardSettings {
        override val sizeInPx: Vec2d = Settings.boardDimens.realSizeInPx
        override val colorset: TileColorset = Settings.tileColorset
        override val boardDimens: BoardDimens = state.boardDimens
      }
    )

    svg.svg(
      svg.idAttr(svgElementId),
      svg.width(Utils.toPx(state.boardDimens.realSizeInPx.x)),
      svg.height(Utils.toPx(state.boardDimens.realSizeInPx.y)),
      tilesComponent,
      inContext { ctx =>
        children <-- pieceComponentsSignal(ctx.ref)
      },
      state.draggingModule.documentBindings
    )
  }
}
