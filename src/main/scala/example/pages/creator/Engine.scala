package example.pages.creator

import com.raquo.laminar.api.L._
import dev.bluepitaya.laminardragging.DragEventKind.End
import dev.bluepitaya.laminardragging.DragEventKind.Move
import dev.bluepitaya.laminardragging.DragEventKind.Start
import dev.bluepitaya.laminardragging.Vec2f
import example.pages.creator.PiecePicker.PiecePicked
import org.scalajs.dom

object Engine {
  def handleEvent(state: State): Observer[PiecePicker.Event] = Observer { e =>
    e match {
      case PiecePicked(p, e) => e.kind match {
          case Start | Move => state
              .draggingPieceState
              .set(Some(DraggingPieceState(p, getPosition(e.e))))
          case End => state.draggingPieceState.set(None)
        }
    }
  }

  private def getPosition(e: dom.PointerEvent): Vec2f = Vec2f(e.pageX, e.pageY)
}
