package example.exp

import cats.effect.IO
import dev.bluepitaya.laminardragging.DragEventKind.End
import dev.bluepitaya.laminardragging.DragEventKind.Move
import dev.bluepitaya.laminardragging.DragEventKind.Start

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
    }
  }

  def onStart(e: PickerPieceDragging): IO[Unit] = {
    val imgPath = ExApp.pieceImgPath(e.color, e.piece)

    IO.println("todo")
  }
}
