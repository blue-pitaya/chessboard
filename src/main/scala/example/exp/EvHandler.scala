package example.exp

import cats.effect.IO
import dev.bluepitaya.laminardragging.DragEventKind.End
import dev.bluepitaya.laminardragging.DragEventKind.Move
import dev.bluepitaya.laminardragging.DragEventKind.Start
import example.game.Vec2d
import org.scalajs.dom
import dev.bluepitaya.laminardragging.Vec2f
import cats.data.OptionT

object EvHandler {
  import ExAppModel._

  def handle(s: State, e: Ev): IO[Unit] = {
    e match {
      case e @ PickerPieceDragging(draggingEvent, piece, color) =>
        draggingEvent.kind match {
          case Start => onStart(s, e)
          case Move  => onStart(s, e)
          case End   => onEnd(s, e)
        }
      case BoardContainerRefChanged(v) => IO {
          s.containerRef.set(Some(v))
        }
      case BoardWidthChanged(v) => IO {
          s.boardSize.update(size => Vec2d(v, size.y))
        }
      case BoardHeightChanged(v) => IO {
          s.boardSize.update(size => Vec2d(size.x, v))
        }
    }
  }

  def onEnd(state: State, e: PickerPieceDragging): IO[Unit] = {
    (
      for {
        _ <- OptionT.liftF(IO(state.draggingPieceState.set(None)))
        canvasSize = state.canvasSize
        boardSize <- OptionT.liftF(IO(state.boardSize.now()))
        containerRef <- OptionT(IO(state.containerRef.now()))
        canvasPos = getRelativePosition(e.e.e, containerRef)
        tilePos <- OptionT
          .fromOption[IO](tileLogicPos(boardSize, canvasSize, canvasPos))
        _ <- OptionT.liftF(placePiece(tilePos, e.color, e.piece, state))
      } yield ()
    ).getOrElse(())
  }

  def placePiece(
      pos: Vec2d,
      color: FigColor,
      piece: Fig,
      state: State
  ): IO[Unit] = IO {
    state.placedPieces.update(v => v.updated(pos, (color, piece)))
  }

  def tileLogicPos(
      boardSize: Vec2d,
      canvasSize: Vec2d,
      canvasPos: Vec2d
  ): Option[Vec2d] = {
    val tileSize = ExBoard.tileSize(boardSize, canvasSize)
    val boardOffset = ExBoard.boardOffset(tileSize, boardSize, canvasSize)
    val onBoardPos = (canvasPos - boardOffset)
    val pos =
      Vec2f(onBoardPos.x.toDouble / tileSize, onBoardPos.y.toDouble / tileSize)

    Option.when(isBetween(pos, Vec2f.zero, toVec2f(boardSize)))(
      invertYAxis(toVec2dRoundedDown(pos), boardSize.y)
    )
  }

  def toVec2f(v: Vec2d): Vec2f = Vec2f(v.x, v.y)

  def isBetween(v: Vec2f, b1: Vec2f, b2: Vec2f): Boolean = v.x >= b1.x &&
    v.y >= b1.y && v.x < b2.x && v.y < b2.y

  def invertYAxis(v: Vec2d, h: Int): Vec2d = Vec2d(v.x, h - v.y - 1)

  def toVec2dRoundedDown(v: Vec2f): Vec2d = Vec2d(v.x.toInt, v.y.toInt)

  /** Get dragging position relative to other element. */
  def getRelativePosition(
      e: dom.PointerEvent,
      container: dom.Element
  ): Vec2d = {
    val rect = container.getBoundingClientRect()
    val x = e.pageX - (rect.x + dom.window.pageXOffset)
    val y = e.pageY - (rect.y + dom.window.pageYOffset)

    Vec2d(x.toInt, y.toInt)
  }

  def onStart(state: State, e: PickerPieceDragging): IO[Unit] = {
    val imgPath = ExApp.pieceImgPath(e.color, e.piece)
    IO(
      state
        .draggingPieceState
        .set(Some(DraggingPieceState(imgPath = imgPath, draggingEvent = e.e)))
    )
  }

}
