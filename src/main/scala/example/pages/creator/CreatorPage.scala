package example.pages.creator

import com.raquo.laminar.api.L._
import org.scalajs.dom
import example.game.Vec2d

object CreatorPage {
  def component(): HtmlElement = {
    // val state = State.init

    val boardState = BoardContainer
      .State(boardLogicSize = Vec2d(20, 20), boardRealSize = Vec2d(800, 800))

    div(cls("m-10"), BoardContainer.component(boardState))
  }

}

object Form {

  def component(state: State, observer: Observer[Event]): HtmlElement = div(
    div(
      child.text <-- state.boardSize.w.signal.map(_.toString()),
      p("Board width"),
      input(
        cls("text-gray-900"),
        controlled(
          value <-- boardWidthSignal(state),
          onInput.map(e => BoardWidthInput(e)) --> observer
        )
      ),
      p("Board height"),
      input(
        cls("text-gray-900"),
        controlled(
          value <-- boardHeightSignal(state),
          onInput.map(e => BoardHeightInput(e)) --> observer
        )
      )
    )
  )

  sealed trait Event
  case class BoardWidthInput(e: dom.Event) extends Event
  case class BoardHeightInput(e: dom.Event) extends Event

  def observer(state: State): Observer[Event] = ???

  private def boardWidthSignal(state: State): Signal[String] = state
    .boardSize
    .w
    .signal
    .map(_.toString())

  private def boardHeightSignal(state: State): Signal[String] = state
    .boardSize
    .h
    .signal
    .map(_.toString())
}
