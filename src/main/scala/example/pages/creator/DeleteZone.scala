package example.pages.creator

import cats.effect.IO
import com.raquo.laminar.api.L._
import example.Utils

object DeleteZone {
  import ExAppModel._

  def component(handler: Ev => IO[Unit]): Element = div(
    cls("bg-stone-800"),
    cls("flex flex-row min-h-[100px] items-center justify-around"),
    p(
      pointerEvents.none,
      styleProp("user-select")("none"),
      "Drop pieces here to remove"
    ),
    onMountCallback(onMountCbEffect(_, handler))
  )

  def onMountCbEffect(
      ctx: MountContext[Element],
      handler: Ev => IO[Unit]
  ): Unit = {
    Utils.catsRun(handler)(RemoveZoneRefChanged(ctx.thisNode.ref))
  }

}
