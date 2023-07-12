package example.pages.creator

import cats.data.OptionT
import cats.effect.IO
import chessboardcore.Model._
import chessboardcore.Vec2d
import com.raquo.laminar.api.L._
import dev.bluepitaya.laminardragging.DragEventKind.End
import dev.bluepitaya.laminardragging.DragEventKind.Move
import dev.bluepitaya.laminardragging.DragEventKind.Start
import dev.bluepitaya.laminardragging.Dragging
import dev.bluepitaya.laminardragging.Vec2f
import org.scalajs.dom

object EvHandler {
  import ExAppModel._

  def handle(state: State, event: Ev): IO[Unit] = {
    event match {
      case e: PickerPieceDragging => e.e.kind match {
          case Start => onStart(state, e)
          case Move  => onStart(state, e)
          case End   => onEnd(state, e)
        }
      case BoardContainerRefChanged(v) => IO {
          state.boardContainerRef.set(Some(v))
        }
      case BoardWidthChanged(v) => IO {
          state.boardSize.update(size => Vec2d(v, size.y))
        }
      case BoardHeightChanged(v) => IO {
          state.boardSize.update(size => Vec2d(size.x, v))
        }
      case e: PlacedPieceDragging => handlePlacedPieceDragging(state, e)
      case RemoveZoneRefChanged(v) =>
        IO(state.removeZoneComponentRef.set(Some(v)))
      case SaveBoardRequested() => IO.println("ok")
    }
  }

  def handlePlacedPieceDragging(
      state: State,
      event: PlacedPieceDragging
  ): IO[Unit] = {
    for {
      pieceOpt <- placedPieceOnPos(event.fromPos, state).value
      _ <- pieceOpt match {
        case None        => IO.unit
        case Some(piece) => handlePieceDragging(piece, state, event)
      }
    } yield ()
  }

  def handlePieceDragging(
      piece: ColoredPiece,
      state: State,
      event: PlacedPieceDragging
  ): IO[Unit] = event.e.kind match {
    case Start => for {
        _ <- IO(piece.isVisible.set(false))
        _ <- handlePieceDragging(
          state,
          event.e,
          ExApp.pieceImgPath(piece.color, piece.piece)
        )
      } yield ()
    case Move => handlePieceDragging(
        state,
        event.e,
        ExApp.pieceImgPath(piece.color, piece.piece)
      )
    case End => onEndPlacedPieceDragging(piece, state, event)
  }

  def onEndPlacedPieceDragging(
      piece: ColoredPiece,
      state: State,
      event: PlacedPieceDragging
  ): IO[Unit] = {
    for {
      _ <- IO(piece.isVisible.set(true))
      _ <- IO(state.draggingPieceState.set(None))
      tilePosOpt <- tileLogicPos(state, event.e).value
      _ <- tilePosOpt match {
        case Some(tilePos) =>
          movePlacedPiece(state, event.fromPos, tilePos, piece)
        case None => IO.unit
      }
      _isOverRemoveZone <- isOverRemoveZone(state, event.e)
      _ <-
        if (_isOverRemoveZone) removePiece(event.fromPos, state)
        else IO.unit
    } yield ()
  }

  def isOverRemoveZone(state: State, event: Dragging.Event): IO[Boolean] = {
    (
      for {
        componentRef <- OptionT(IO(state.removeZoneComponentRef.now()))
        result <- OptionT
          .some[IO](isPointerInsideElement(event.e, componentRef))
      } yield (result)
    ).getOrElse(false)
  }

  def movePlacedPiece(
      state: State,
      fromPos: Vec2d,
      toPos: Vec2d,
      piece: ColoredPiece
  ): IO[Unit] = for {
    _ <- removePiece(fromPos, state)
    _ <- placePiece(toPos, piece.color, piece.piece, state)
  } yield ()

  def removePiece(pos: Vec2d, state: State): IO[Unit] =
    IO(state.placedPieces.update(v => v.removed(pos)))

  def placedPieceOnPos(pos: Vec2d, state: State): OptionT[IO, ColoredPiece] =
    for {
      placedPieces <- OptionT.liftF(IO(state.placedPieces.now()))
      pieceOnPos <- OptionT.fromOption[IO](placedPieces.get(pos))
    } yield (pieceOnPos)

  def tileLogicPos(state: State, e: Dragging.Event): OptionT[IO, Vec2d] = for {
    boardSize <- OptionT.liftF(IO(state.boardSize.now()))
    containerRef <- OptionT(IO(state.boardContainerRef.now()))
    canvasPos = getRelativePosition(e.e, containerRef)
    canvasSize = state.canvasSize
    tilePos <- OptionT
      .fromOption[IO](tileLogicPos(boardSize, canvasSize, canvasPos))
  } yield (tilePos)

  def onEnd(state: State, e: PickerPieceDragging): IO[Unit] = {
    (
      for {
        _ <- OptionT.liftF(IO(state.draggingPieceState.set(None)))
        tilePos <- tileLogicPos(state, e.e)
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
    state
      .placedPieces
      .update(v => v.updated(pos, ColoredPiece(color, piece, Var(true))))
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

  def isPointerInsideElement(
      e: dom.PointerEvent,
      container: dom.Element
  ): Boolean = {
    val rect = container.getBoundingClientRect()
    val x = e.pageX - (rect.x + dom.window.pageXOffset)
    val y = e.pageY - (rect.y + dom.window.pageYOffset)

    (x >= 0) && (y >= 0) && (x < rect.width) && (y < rect.height)
  }

  def onStart(state: State, e: PickerPieceDragging): IO[Unit] = {
    val imgPath = ExApp.pieceImgPath(e.color, e.piece)
    handlePieceDragging(state, e.e, imgPath)
  }

  def handlePieceDragging(
      state: State,
      draggingEvent: Dragging.Event,
      imgPath: String
  ): IO[Unit] = IO(
    state
      .draggingPieceState
      .set(
        Some(
          DraggingPieceState(imgPath = imgPath, draggingEvent = draggingEvent)
        )
      )
  )

}
