package example.components

import chessboardcore.Model
import chessboardcore.Vec2d
import com.raquo.laminar.api.L._
import dev.bluepitaya.laminardragging.Dragging
import example.AppModel._
import example.Misc
import example.components.Logic
import org.scalajs.dom

object BoardComponent {
  type BoardSize = Vec2d
  type BoardPos = Vec2d
  type IsFlipped = Boolean

  case class PieceUiModel(piece: Model.Piece, isVisible: Var[Boolean])

  sealed trait Event
  case class ElementRefChanged(v: dom.Element) extends Event
  case class PieceDragging(e: Dragging.Event, fromPos: Vec2d) extends Event

  case class Data(
      canvasSize: Vec2d,
      boardSize: Signal[Vec2d],
      placedPieces: Signal[Map[Vec2d, PieceUiModel]],
      dm: DM,
      highlightedTiles: Signal[Set[Vec2d]],
      isFlipped: Signal[Boolean]
  )

  def create(data: Data, handler: Observer[Event]): Element = {
    val tileComponent = (boardSize: BoardSize, pos: BoardPos, f: IsFlipped) => {
      TileComponent.create(
        TileComponent.Data(
          pos = pos,
          boardSize = boardSize,
          canvasSize = data.canvasSize,
          isHighlighted = data.highlightedTiles.map(_.contains(pos)),
          canvasPos = Logic.tileCanvasPos(data.canvasSize, boardSize, pos, f)
        )
      )
    }
    val tileComponentsSignal = data
      .boardSize
      .combineWith(data.isFlipped)
      .map { case (boardSize, isFlipped) =>
        val tileLogicPositions = Vec2d.matrix(boardSize)
        tileLogicPositions.map(pos => tileComponent(boardSize, pos, isFlipped))
      }

    val placedPiecesSignal = data
      .placedPieces
      .signal
      .combineWith(data.boardSize)
      .combineWith(data.isFlipped)
      .map { case (placedPieces, boardSize, isFlipped) =>
        placedPiecesOnBoard(placedPieces, boardSize).map {
          case (pos, pieceUiModel) =>
            val imgPath = Misc.pieceImgPath(pieceUiModel.piece)
            placedPieceComponent(
              Logic.tileCanvasPos(data.canvasSize, boardSize, pos, isFlipped),
              Logic.tileSize(boardSize, data.canvasSize),
              imgPath,
              pieceUiModel.isVisible.signal,
              placedPieceDraggingBindings(pos, data.dm, handler)
            )
        }
      }

    svg.svg(
      // TODO: should take canvasSize, but there is no styleProp for svg
      // and interpolation string for tailwind cls is broken
      svg.cls("min-w-[800px] h-[800px] bg-stone-800"),
      svg.g(children <-- tileComponentsSignal),
      svg.g(children <-- placedPiecesSignal),
      onMountCallback(ctx =>
        handler.onNext(ElementRefChanged(ctx.thisNode.ref))
      )
    )
  }

  private def placedPieceDraggingBindings(
      fromPos: Vec2d,
      dm: DM,
      handler: Observer[Event]
  ): Seq[Binder.Base] = {
    val draggingId = DraggingId.PlacedPiece(fromPos)

    dm.componentBindings(draggingId) ++
      Seq(
        dm.componentEvents(draggingId).map(e => PieceDragging(e, fromPos)) -->
          handler
      )
  }

  private def placedPiecesOnBoard(
      placedPieces: Map[Vec2d, PieceUiModel],
      boardSize: Vec2d
  ): List[(Vec2d, PieceUiModel)] = {
    placedPieces
      .toList
      .collect {
        case (pos, piece) if Logic.isPosOnBoard(pos, boardSize) => (pos, piece)
      }
  }

  private def placedPieceComponent(
      pos: Vec2d,
      tileSize: Int,
      pieceImgPath: String,
      isVisibleSignal: Signal[Boolean],
      draggingBindings: Seq[Binder.Base]
  ): Element = {
    svg.image(
      svg.x(pos.x.toString()),
      svg.y(pos.y.toString()),
      svg.width(tileSize.toString()),
      svg.height(tileSize.toString()),
      svg.href <--
        isVisibleSignal.map {
          case true => pieceImgPath
          // TODO: quick hack
          case false => ""
        },
      draggingBindings
    )
  }
}
