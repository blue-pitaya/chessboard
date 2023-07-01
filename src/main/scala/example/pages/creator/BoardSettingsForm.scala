package example.pages.creator

import org.scalajs.dom

object BoardSettingsForm {
  sealed trait Event
  case class BoardWidthInput(e: dom.Event) extends Event
  case class BoardHeightInput(e: dom.Event) extends Event

  // def component(state: State, observer: Observer[Event]): HtmlElement = div(
  //  div(
  //    child.text <-- state.boardSize.w.signal.map(_.toString()),
  //    p("Board width"),
  //    input(
  //      cls("text-gray-900"),
  //      controlled(
  //        value <-- boardWidthSignal(state),
  //        onInput.map(e => BoardWidthInput(e)) --> observer
  //      )
  //    ),
  //    p("Board height"),
  //    input(
  //      cls("text-gray-900"),
  //      controlled(
  //        value <-- boardHeightSignal(state),
  //        onInput.map(e => BoardHeightInput(e)) --> observer
  //      )
  //    )
  //  )
  // )

  // private def boardWidthSignal(state: State): Signal[String] = state
  //  .boardSize
  //  .w
  //  .signal
  //  .map(_.toString())

  // private def boardHeightSignal(state: State): Signal[String] = state
  //  .boardSize
  //  .h
  //  .signal
  //  .map(_.toString())
}
