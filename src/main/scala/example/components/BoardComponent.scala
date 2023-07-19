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

  case class PieceUiModel(piece: Model.Piece, isVisible: Var[Boolean])

  sealed trait Event
  case class ElementRefChanged(v: dom.Element) extends Event
  case class PieceDragging(e: Dragging.Event, fromPos: Vec2d) extends Event

  case class Data(
      canvasSize: Vec2d,
      boardSize: Signal[Vec2d],
      placedPieces: Signal[Map[Vec2d, PieceUiModel]],
      dm: DM
  )

  def create(data: Data, handler: Observer[Event]): Element = {
    val canvasSize = data.canvasSize

    val _tileSize = (bs: BoardSize) => Logic.tileSize(bs, canvasSize)
    val _tileCanvasPos =
      (bs: BoardSize, pos: BoardPos) => Logic.tileCanvasPos(canvasSize, bs, pos)

    val tileComponent = (boardSize: BoardSize, pos: BoardPos) => {
      TileComponent.create(
        TileComponent.Data(
          pos = pos,
          boardSize = boardSize,
          canvasSize = data.canvasSize,
          isHighlighted = Val(false)
        )
      )
    }
    val tilesSignal = data
      .boardSize
      .map(bs => tileComponents((pos: Vec2d) => tileComponent(bs, pos), bs))

    val _placedPieceDraggingBindings =
      (pos: BoardPos) => placedPieceDraggingBindings(pos, data.dm, handler)

    val _placedPiecesSignal = placedPiecesSignal(
      data.boardSize,
      data.placedPieces,
      _tileSize,
      _tileCanvasPos,
      _placedPieceDraggingBindings
    )

    svg.svg(
      // TODO: should take canvasSize, but there is no styleProp for svg
      // and interpolation string for tailwind cls is broken
      svg.cls("min-w-[800px] h-[800px] bg-stone-800"),
      svg.g(children <-- tilesSignal),
      svg.g(children <-- _placedPiecesSignal),
      onMountCallback(ctx =>
        handler.onNext(ElementRefChanged(ctx.thisNode.ref))
      )
    )
  }

  def placedPieceDraggingBindings(
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

  def placedPiecesSignal(
      boardSizeSignal: Signal[Vec2d],
      placedPiecesSignal: Signal[Map[Vec2d, PieceUiModel]],
      tileSize: BoardSize => Int,
      canvasPos: (BoardSize, BoardPos) => Vec2d,
      draggingBindings: BoardPos => Seq[Binder.Base]
  ): Signal[List[Element]] = {
    placedPiecesSignal
      .combineWith(boardSizeSignal)
      .map { case (placedPieces, boardSize) =>
        placedPiecesOnBoard(placedPieces, boardSize).map {
          case (pos, pieceUiModel) =>
            val imgPath = Misc.pieceImgPath(pieceUiModel.piece)
            placedPieceComponent(
              canvasPos(boardSize, pos),
              tileSize(boardSize),
              imgPath,
              pieceUiModel.isVisible.signal,
              draggingBindings(pos)
            )
        }
      }
  }

  def placedPiecesOnBoard(
      placedPieces: Map[Vec2d, PieceUiModel],
      boardSize: Vec2d
  ): List[(Vec2d, PieceUiModel)] = {
    placedPieces
      .toList
      .collect {
        case (pos, piece) if isPosOnBoard(pos, boardSize) => (pos, piece)
      }
  }

  def isPosOnBoard(pos: Vec2d, boardSize: Vec2d): Boolean =
    isBetween(pos, Vec2d.zero, boardSize)

  // TODO: dups in 2 places (search for isInside...)
  def isBetween(v: Vec2d, b1: Vec2d, b2: Vec2d): Boolean = v.x >= b1.x &&
    v.y >= b1.y && v.x < b2.x && v.y < b2.y

  def placedPieceComponent(
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

  def tileComponents(
      tileComponent: Vec2d => Element,
      boardSize: Vec2d
  ): List[Element] = {
    val tileLogicPositions = Vec2d.matrix(boardSize)
    tileLogicPositions.map(tileComponent)
  }

}
