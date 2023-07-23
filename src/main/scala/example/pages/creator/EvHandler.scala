package example.pages.creator

import cats.data.OptionT
import cats.effect.IO
import chessboardcore.HttpModel
import chessboardcore.Model._
import chessboardcore.Vec2d
import com.raquo.laminar.api.L._
import dev.bluepitaya.laminardragging.DragEventKind.End
import dev.bluepitaya.laminardragging.DragEventKind.Move
import dev.bluepitaya.laminardragging.DragEventKind.Start
import dev.bluepitaya.laminardragging.Dragging
import example.AppModel
import example.AppRouter
import example.Main
import example.Misc
import example.PageKey
import example.components.DraggingPiece.DraggingPieceState
import example.components.Logic
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe._
import org.http4s.client.Client
import org.scalajs.dom

object EvHandler {
  import ExAppModel._
  import example.components.BoardComponent._

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
      case SaveBoardRequested() => saveBoard(state, Main.client)
      case CreateGameUsingCurrentBoardRequested() =>
        createGame(state, Main.client)
    }
  }

  def createGame(state: State, httpClient: Client[IO]): IO[Unit] = {
    val uri = AppModel.ApiPath / "game"
    val data = HttpModel.CreateGame_In(board(state))
    val request = Request[IO](Method.PUT, uri).withEntity(data.asJson)

    for {
      resp <- httpClient.expect[HttpModel.CreateGame_Out](request)
      _ <- redirectToGame(resp.id)
    } yield ()
  }

  def redirectToGame(id: String): IO[Unit] =
    IO(AppRouter.router.pushState(PageKey.Game(id)))

  def board(state: State): Board = {
    val boardSize = state.boardSize.now()
    val piecesOnBoard = state
      .placedPieces
      .now()
      .collect {
        case (pos, pieceUiModel) if Logic.isPosOnBoard(pos, boardSize) =>
          (pos, pieceUiModel.piece)
      }

    Board(size = boardSize, pieces = piecesOnBoard)
  }

  def saveBoard(state: State, httpClient: Client[IO]): IO[Unit] = {
    val boardSize = state.boardSize.now()
    val pieces = state
      .placedPieces
      .now()
      .toList
      .map { case (pos, pieceUiModel) =>
        PlacedPiece(pos = pos, piece = pieceUiModel.piece)
      }
    val data = HttpModel
      .CreateChessboard_In(boardSize = boardSize, pieces = pieces)
    val uri = AppModel.ApiPath / "chessboard"
    val request = Request[IO](Method.PUT, uri).withEntity(data.asJson)

    httpClient.expect[Unit](request)
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
      pieceUiModel: PieceUiModel,
      state: State,
      event: PlacedPieceDragging
  ): IO[Unit] = event.e.kind match {
    case Start => for {
        _ <- IO(pieceUiModel.isVisible.set(false))
        _ <- handlePieceDragging(
          state,
          event.e,
          Misc.pieceImgPath(pieceUiModel.piece)
        )
      } yield ()
    case Move =>
      handlePieceDragging(state, event.e, Misc.pieceImgPath(pieceUiModel.piece))
    case End => onEndPlacedPieceDragging(pieceUiModel, state, event)
  }

  def onEndPlacedPieceDragging(
      piece: PieceUiModel,
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
      pieceUiModel: PieceUiModel
  ): IO[Unit] = for {
    _ <- removePiece(fromPos, state)
    _ <- placePiece(toPos, pieceUiModel.piece, state)
  } yield ()

  def removePiece(pos: Vec2d, state: State): IO[Unit] =
    IO(state.placedPieces.update(v => v.removed(pos)))

  def placedPieceOnPos(pos: Vec2d, state: State): OptionT[IO, PieceUiModel] =
    for {
      placedPieces <- OptionT.liftF(IO(state.placedPieces.now()))
      pieceOnPos <- OptionT.fromOption[IO](placedPieces.get(pos))
    } yield (pieceOnPos)

  def tileLogicPos(state: State, e: Dragging.Event): OptionT[IO, Vec2d] = for {
    boardSize <- OptionT.liftF(IO(state.boardSize.now()))
    containerRef <- OptionT(IO(state.boardContainerRef.now()))
    canvasPos = getRelativePosition(e.e, containerRef)
    canvasSize = AppModel.DefaultBoardCanvasSize
    tilePos <- OptionT.fromOption[IO](
      Logic.tileLogicPos(boardSize, canvasSize, canvasPos, false)
    )
  } yield (tilePos)

  def onEnd(state: State, e: PickerPieceDragging): IO[Unit] = {
    (
      for {
        _ <- OptionT.liftF(IO(state.draggingPieceState.set(None)))
        tilePos <- tileLogicPos(state, e.e)
        _ <- OptionT.liftF(placePiece(tilePos, e.piece, state))
      } yield ()
    ).getOrElse(())
  }

  def placePiece(pos: Vec2d, piece: Piece, state: State): IO[Unit] = IO {
    state
      .placedPieces
      .update(v => v.updated(pos, PieceUiModel(piece, Var(true))))
  }

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
    val imgPath = Misc.pieceImgPath(e.piece)
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
