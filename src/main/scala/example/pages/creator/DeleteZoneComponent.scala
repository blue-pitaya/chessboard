package example.pages.creator

import com.raquo.laminar.api.L._
import org.scalajs.dom

object DeleteZoneComponent {
  sealed trait Event
  case class RefChanged(v: dom.Element) extends Event

  def create(handler: Observer[Event]): Element = div(
    cls("flex flex-row bg-stone-800 min-h-[100px] items-center justify-around"),
    p(
      pointerEvents.none,
      styleProp("user-select")("none"),
      "Drop pieces here to remove"
    ),
    onMountCallback { ctx =>
      handler.onNext(RefChanged(ctx.thisNode.ref))
    }
  )
}
