package example.pages.creator

import com.raquo.laminar.api.L._
import example.pages.creator.PiecePicker.PiecePicked
import dev.bluepitaya.laminardragging.DragEventKind.End
import dev.bluepitaya.laminardragging.DragEventKind.Move
import dev.bluepitaya.laminardragging.DragEventKind.Start

object Engine {
  def handleEvent(state: State): Observer[PiecePicker.Event] = Observer { e =>
    e match {
      case PiecePicked(e) => e.kind match {
          case Start => println("start")
          case Move  => println("move")
          case End   => println("end")
        }
      // case PieceDraggingStart(piece, position) => state
      //    .draggingPieceState
      //    .set(Some(DraggingPieceState(piece = piece, position = ???)))
    }
    ()
  }
}
