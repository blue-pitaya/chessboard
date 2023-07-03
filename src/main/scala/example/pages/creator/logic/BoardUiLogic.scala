package example.pages.creator.logic

import com.raquo.laminar.api.L._
import dev.bluepitaya.laminardragging.DragEventKind.End
import dev.bluepitaya.laminardragging.DragEventKind.Move
import dev.bluepitaya.laminardragging.DragEventKind.Start
import dev.bluepitaya.laminardragging.Dragging
import dev.bluepitaya.laminardragging.Vec2f
import example.game.Vec2d
import example.pages.creator.Models
import org.scalajs.dom
import example.pages.creator.logic.DraggingId.PieceOnBoardId
import example.pages.creator.logic.DraggingId.PieceOnPicker

sealed trait DraggingId
object DraggingId {
  // TODO: piece here is not needed!
  case class PieceOnBoardId(fromPos: Vec2d, p: Models.Piece) extends DraggingId
  case class PieceOnPicker(p: Models.Piece) extends DraggingId
}

object BoardUiLogic {
  case class DraggingPieceState(piece: Models.Piece, position: Vec2f)

  case class Tile(pos: Vec2d, isHovered: Var[Boolean])

  case class PieceOnBoard(
      anchorPos: Vec2d,
      piece: Models.Piece,
      isVisible: Var[Boolean]
  )

  case class State(
      boardSize: Var[Vec2d],
      canvasSize: Vec2d,
      tileMaxSize: Int,
      tiles: Var[List[Tile]],
      containerRef: Var[Option[dom.Element]],
      draggingPieceState: Var[Option[DraggingPieceState]],
      piecesOnBoard: Var[Map[Vec2d, PieceOnBoard]]
  )

  object State {
    def default(boardSize: Vec2d) = State(
      boardSize = Var(boardSize),
      canvasSize = Vec2d(800, 800),
      tileMaxSize = 100,
      tiles = Var(createTiles(boardSize)),
      containerRef = Var(None),
      draggingPieceState = Var(None),
      piecesOnBoard = Var(Map())
    )
  }

  sealed trait Event
  case class PointerMove(pos: Vec2f) extends Event
  case class ContainerChanged(el: dom.Element) extends Event
  // TODO: remve p
  case class PieceDragging(id: DraggingId, p: Models.Piece, e: Dragging.Event)
      extends Event

  def observer(s: State): Observer[Event] = Observer { e =>
    e match {
      case PointerMove(pos)     => () // onPointerMove(s, pos)
      case ContainerChanged(el) => s.containerRef.set(Some(el))
      case e @ PieceDragging(id, piece, draggingEvent) =>
        id match {
          case id: PieceOnBoardId =>
            handleBoardPieceDragging(s, draggingEvent, id)
          case _: PieceOnPicker => hanlePickerPieceDragging(s, e)
        }
        handleDraggingImage(s, piece, draggingEvent)
    }
  }

  def hanlePickerPieceDragging(s: State, pe: PieceDragging): Unit =
    s.containerRef.now() match {
      case None => ()
      case Some(container) =>
        val relPos = Dragging.getRelativePosition(pe.e, container)
        pe.e.kind match {
          case Start => ()
          case Move  => ()
          case End =>
            val pos = tileLogicPos(s, relPos)
            pos.foreach(pos => putPiece(s, pe.p, pos))
        }
    }

  def handleBoardPieceDragging(
      s: State,
      e: Dragging.Event,
      id: DraggingId.PieceOnBoardId
  ): Unit = e.kind match {
    case Start => s
        .piecesOnBoard
        .now()
        .get(id.fromPos)
        .foreach { pob =>
          pob.isVisible.set(false)
        }
    case Move => ()
    case End => s.containerRef.now() match {
        case None => ()
        case Some(container) =>
          val boardPos =
            tileLogicPos(s, Dragging.getRelativePosition(e, container))
          boardPos match {
            case None => s
                .piecesOnBoard
                .now()
                .get(id.fromPos)
                .foreach { pob =>
                  pob.isVisible.set(true)
                }
            case Some(pos) => s
                .piecesOnBoard
                .update(v =>
                  v.removed(id.fromPos)
                    .updated(
                      pos,
                      PieceOnBoard(
                        anchorPos = pos,
                        piece = id.p,
                        isVisible = Var(true)
                      )
                    )
                )
          }

      }

  }

  def putPiece(s: State, p: Models.Piece, pos: Vec2d): Unit = {
    val pieceOnBoard =
      PieceOnBoard(anchorPos = pos, piece = p, isVisible = Var(true))

    s.piecesOnBoard.update(v => v.updated(pos, pieceOnBoard))
  }

  def handleDraggingImage(
      s: State,
      piece: Models.Piece,
      e: Dragging.Event
  ): Unit = e.kind match {
    case Start | Move => s
        .draggingPieceState
        .set(Some(DraggingPieceState(piece, getPosition(e.e))))
    case End => s.draggingPieceState.set(None)
  }

  private def getPosition(e: dom.PointerEvent): Vec2f = Vec2f(e.pageX, e.pageY)

  def createTiles(boardSize: Vec2d): List[Tile] = (0.until(boardSize.x))
    .map(x => (0.until(boardSize.y)).map(y => Vec2d(x, y)))
    .flatten
    .toList
    .map(pos => Tile(pos = pos, isHovered = Var(false)))

  def onPointerMove(s: State, pos: Vec2f): Unit = {
    for {
      tileLogicPos <- tileLogicPos(s, pos)
      tile <- s.tiles.now().find(t => t.pos == tileLogicPos)
      _ = {
        tile.isHovered.set(true)
      }
    } yield ()
  }

  def tileLogicPos(s: State, realPos: Vec2f): Option[Vec2d] = {
    val boardOffset = toVec2f(getBoardOffset(s))
    val tileSize = getTileSize(s)
    val totalTileSize = toVec2f(totalTilesSize(s))
    val onBoardPos = (realPos - boardOffset)
    val p = div(onBoardPos, tileSize)
    val boardSize = s.boardSize.now()

    Option.when(isBetween(p, Vec2f.zero, toVec2f(boardSize)))(
      invertYAxis(toVec2dRoundedDown(p), boardSize.y)
    )
  }

  def invertYAxis(v: Vec2d, h: Int): Vec2d = Vec2d(v.x, h - v.y - 1)

  def div(v: Vec2f, ord: Double): Vec2f = Vec2f(v.x / ord, v.y / ord)

  def toVec2dRoundedDown(v: Vec2f): Vec2d = Vec2d(v.x.toInt, v.y.toInt)

  def isBetween(v: Vec2f, b1: Vec2f, b2: Vec2f): Boolean = v.x >= b1.x &&
    v.y >= b1.y && v.x < b2.x && v.y < b2.y

  def divLikeOrd(v1: Vec2f, v2: Vec2f): Vec2f = Vec2f(v1.x / v2.x, v1.y / v2.y)

  def toVec2f(v: Vec2d): Vec2f = Vec2f(v.x, v.y)

  def tileLogicPositions(boardLogicSize: Vec2d): List[Vec2d] =
    (0.until(boardLogicSize.x))
      .map(x => (0.until(boardLogicSize.y)).map(y => Vec2d(x, y)))
      .flatten
      .toList

  def tileCanvasPos(s: State, tileLogicPos: Vec2d): Vec2d = {
    val tileSize = getTileSize(s)
    val boardOffset = getBoardOffset(s)
    val x = tileLogicPos.x * tileSize
    val y = (s.canvasSize.y - tileSize) - (tileLogicPos.y * tileSize)

    Vec2d(boardOffset.x + x, y - boardOffset.y)
  }

  def getBoardOffset(s: State): Vec2d = {
    val tileSize = getTileSize(s)

    (s.canvasSize - totalTilesSize(s)) / 2
  }

  def totalTilesSize(s: State): Vec2d = {
    val tileSize = getTileSize(s)

    s.boardSize.now() * tileSize
  }

  def getTileSize(boardState: State): Int = {
    val maxSize = 100
    val x = boardState.canvasSize.x / boardState.boardSize.now().x
    val y = boardState.canvasSize.y / boardState.boardSize.now().y

    Math.min(Math.min(maxSize, x), Math.min(maxSize, y))
  }

  def tileHexColor(logicPos: Vec2d, isHovered: Boolean): String = {
    val blackTileColor = "#b58863"
    val whiteTileColor = "#f0d9b5"
    val hoveredColor = "#ff00ff"
    // if (isHovered) hoveredColor
    if ((logicPos.x + logicPos.y) % 2 == 0) blackTileColor
    else whiteTileColor
  }

  import example.pages.creator.Models.PieceKind._
  import example.pages.creator.Models.PieceColor._
  def pieceImgPath(piece: Models.Piece): String = {
    val piecePart = piece.kind match {
      case Queen  => "queen"
      case Rook   => "rook"
      case Pawn   => "pawn"
      case Bishop => "bishop"
      case King   => "king"
      case Knight => "knight"
    }
    val colorPart = piece.color match {
      case Black => "black"
      case White => "white"
    }

    s"/pieces/${colorPart}-${piecePart}.png"
  }
}
