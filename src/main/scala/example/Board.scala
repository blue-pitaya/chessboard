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
    val pieceObjs = Var[List[PieceObj]](List())

    val boardSettings = new BoardSettings {
      override val sizeInPx: Vec2d = Settings.boardDimens.realSizeInPx
      override val colorset: TileColorset = Settings.tileColorset
      override val boardDimens: BoardDimens =
        Settings.boardDimens // FIXME: rewrite to boardSettings
    }

    def moveToBack(id: String): Unit = pieceObjs.update { v =>
      val nextValue = for {
        po <- v.find(_.id == id)
        rest = v.filter(_.id != id)
      } yield (rest :+ po)

      nextValue.getOrElse(v)
    }

    val piecesComponentSignal = pieceObjs
      .signal
      .withCurrentValueOf(gameState)
      .map { case (pieceObjs, gs) =>
        pieceObjs
          .map { po =>
            Piece.component(
              po,
              gs,
              draggingModule,
              highlightedTiles.set,
              boardSettings,
              gameState.set,
              moveToBack
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
      draggingModule.documentBindings,
      gameState.signal.map(gs => PieceObj.fromPieces(gs.pieces)) --> pieceObjs
    )
  }
}
