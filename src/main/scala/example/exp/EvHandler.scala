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
          case Start => onStart(e)
          case Move  => IO.unit
          case End   => IO.println("end")
        }
      case BoardContainerRefChanged(v) => IO.unit
      case BoardWidthChanged(v) => IO {
          s.boardSize.update(size => Vec2d(v, size.y))
        }
      case BoardHeightChanged(v) => IO {
          s.boardSize.update(size => Vec2d(size.x, v))
        }
    }
  }

  def onStart(e: PickerPieceDragging): IO[Unit] = {
    val imgPath = ExApp.pieceImgPath(e.color, e.piece)

    IO.println("todo")
  }
}
