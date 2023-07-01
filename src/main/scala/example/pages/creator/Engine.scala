package example.pages.creator

import com.raquo.laminar.api.L._

object Engine {
  def handleEvent(state: State): Observer[PiecePicker.Event] = Observer { e =>
    ()
  }
}
