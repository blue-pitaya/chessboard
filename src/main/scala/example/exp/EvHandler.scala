package example.exp

import cats.effect.IO
import dev.bluepitaya.laminardragging.DragEventKind.End
import dev.bluepitaya.laminardragging.DragEventKind.Move
import dev.bluepitaya.laminardragging.DragEventKind.Start
import example.game.Vec2d

object EvHandler {
  import ExAppModel._

  def handle(s: State, e: Ev): IO[Unit] = {
    e match {
      case e @ PickerPieceDragging(draggingEvent, piece, color) =>
        draggingEvent.kind match {
          case Start => onStart(s, e)
          case Move  => onStart(s, e)
          case End   => onEnd(s)
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

  def onEnd(state: State): IO[Unit] = IO {
    state.draggingPieceState.set(None)
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
