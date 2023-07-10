package example.components

import com.raquo.laminar.api.L._
import example.Utils
import example.components.Tiles
import example.game.GameState
import chessboardcore.Vec2d

case class TileColorset(dark: String, light: String)

trait BoardSettings {
  val sizeInPx: Vec2d
  val colorset: TileColorset
}

object Board {
  val svgElementId = "chessboard"
  val boardSizeInPx = Vec2d(800, 800)
  val tileColorset = TileColorset(dark = "#b58863", light = "#f0d9b5")

  def component() = {
    // val draggingModule = Dragging.createModule[String]()
    val highlightedTiles = Var(Set[Vec2d]())
    val gameState = Var[GameState](GameState.standardBoard)
    val pieceObjs = Var[List[PieceObj]](List())

    val boardSettings = new BoardSettings {
      override val sizeInPx: Vec2d = boardSizeInPx
      override val colorset: TileColorset = tileColorset
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
              // draggingModule,
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
      svg.width(Utils.toPx(boardSettings.sizeInPx.x)),
      svg.height(Utils.toPx(boardSettings.sizeInPx.y)),
      tilesComponent,
      children <-- piecesComponentSignal,
      // draggingModule.documentBindings,
      gameState.signal.map(gs => PieceObj.fromPieces(gs.pieces)) --> pieceObjs
    )
  }
}
