package example.pages.creator

import com.raquo.laminar.api.L._
import example.Styles

object GameSettingsComponent {
  import CreatorPageModel._

  // TODO: rename to create
  def component(state: State, handler: Observer[Event]): Element = {
    val widthSignal = state.boardSize.signal.map(_.x.toString())
    val heightSignal = state.boardSize.signal.map(_.y.toString())

    div(
      cls("w-[200px] flex flex-col bg-stone-800 p-3 gap-4"),
      "Board width",
      input(
        cls("text-gray-900"),
        typ("number"),
        controlled(
          value <-- widthSignal,
          onInput.mapToValue.map(v => BoardWidthChanged(v.toInt)) --> handler
        )
      ),
      "Board height",
      input(
        cls("text-gray-900"),
        typ("number"),
        controlled(
          value <-- heightSignal,
          onInput.mapToValue.map(v => BoardHeightChanged(v.toInt)) --> handler
        )
      ),
      button(
        Styles.btnCls,
        "Create game",
        onClick.mapTo(CreateGameUsingCurrentBoardRequested()) --> handler
      )
    )
  }

}
