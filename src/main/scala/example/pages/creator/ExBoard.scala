package example.pages.creator

import cats.effect.IO
import chessboardcore.Vec2d
import com.raquo.laminar.api.L._
import org.scalajs.dom
import example.Utils
import dev.bluepitaya.laminardragging.Dragging
import example.Misc
import example.AppModel._
import chessboardcore.Model
import example.components.Logic

object BoardModel {
  type BoardSize = Vec2d
  type BoardPos = Vec2d

  case class PieceUiModel(piece: Model.Piece, isVisible: Var[Boolean])

  case class Data(
      canvasSize: Vec2d,
      boardSize: Signal[Vec2d],
      placedPieces: Signal[Map[Vec2d, PieceUiModel]],
      dm: DM
  )

  sealed trait Event
  case class ElementRefChanged(v: dom.Element) extends Event
  case class PieceDragging(e: Dragging.Event, fromPos: Vec2d) extends Event
}

object ExBoard {
  import BoardModel._

  def component(data: Data, handler: Event => IO[Unit]): Element = {
    val canvasSize = data.canvasSize

    val _tileSize = (bs: BoardSize) => Logic.tileSize(bs, canvasSize)
    val _tileCanvasPos =
      (bs: BoardSize) => (pos: BoardPos) => tileCanvasPos(canvasSize, bs, pos)
    val _tileComponent = (boardSize: BoardSize) =>
      (pos: BoardPos) => {
        val tileSize = _tileSize(boardSize)
        tileComponent(_tileCanvasPos(boardSize), tileSize, pos)
      }
    val _tileComponents =
      (bs: BoardSize) => tileComponents(_tileComponent(bs), bs)
    val _tilesSignal = tilesSignal(_tileComponents, data.boardSize)
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
      svg.g(children <-- _tilesSignal),
      svg.g(children <-- _placedPiecesSignal),
      onMountCallback(ctx =>
        Utils.catsRun(handler)(ElementRefChanged(ctx.thisNode.ref))
      )
    )
  }

  def placedPieceDraggingBindings(
      fromPos: Vec2d,
      dm: DM,
      handler: Event => IO[Unit]
  ): Seq[Binder.Base] = {
    val draggingId = DraggingId.PlacedPiece(fromPos)
    dm.componentBindings(draggingId) ++
      Seq(
        dm.componentEvents(draggingId).map(e => PieceDragging(e, fromPos)) -->
          PiecePicker.catsRunObserver(handler)
      )
  }

  def placedPiecesSignal(
      boardSizeSignal: Signal[Vec2d],
      placedPiecesSignal: Signal[Map[Vec2d, PieceUiModel]],
      tileSize: BoardSize => Int,
      canvasPos: BoardSize => BoardPos => Vec2d,
      draggingBindings: BoardPos => Seq[Binder.Base]
  ): Signal[List[Element]] = {
    placedPiecesSignal
      .combineWith(boardSizeSignal)
      .map { case (placedPieces, boardSize) =>
        placedPiecesOnBoard(placedPieces, boardSize).map {
          case (pos, pieceUiModel) =>
            val imgPath = Misc.pieceImgPath(pieceUiModel.piece)
            placedPieceComponent(
              canvasPos(boardSize)(pos),
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

  def tilesSignal(
      tileComponents: Vec2d => List[Element],
      size: Signal[Vec2d]
  ): Signal[List[Element]] = size.map(tileComponents)

  def tileComponents(
      tileComponent: Vec2d => Element,
      boardSize: Vec2d
  ): List[Element] = {
    val tileLogicPositions = Vec2d.matrix(boardSize)
    tileLogicPositions.map(tileComponent)
  }

  def tileComponent(
      tileCanvasPos: Vec2d => Vec2d,
      tileSize: Int,
      logicPos: Vec2d
  ): Element = {
    val pos = tileCanvasPos(logicPos)
    val size = tileSize
    val bgColor = tileColor(logicPos)

    svg.rect(
      svg.x(pos.x.toString()),
      svg.y(pos.y.toString()),
      svg.width(size.toString()),
      svg.height(size.toString()),
      svg.fill(bgColor)
    )
  }

  def tileCanvasPos(canvasSize: Vec2d, boardSize: Vec2d, pos: Vec2d): Vec2d = {
    val _tileSize = Logic.tileSize(boardSize, canvasSize)
    val _boardOffset = boardOffset(_tileSize, boardSize, canvasSize)
    val x = pos.x * _tileSize
    val y = (canvasSize.y - _tileSize) - (pos.y * _tileSize)

    Vec2d(_boardOffset.x + x, y - _boardOffset.y)
  }

  def boardOffset(tileSize: Int, boardSize: Vec2d, canvasSize: Vec2d): Vec2d =
    (canvasSize - (boardSize * tileSize)) / 2

  def tileColor(pos: Vec2d): String = {
    val blackTileColor = "#b58863"
    val whiteTileColor = "#f0d9b5"

    if ((pos.x + pos.y) % 2 == 0) blackTileColor
    else whiteTileColor
  }

}
