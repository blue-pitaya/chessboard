package example.pages.creator.components

import com.raquo.laminar.api.L._
import example.pages.creator.logic.BoardUiLogic
import example.pages.creator.logic.SignalProvider
import example.exp.ExBoardForm

object BoardSettingsComponent {
  import Styles._
  import SignalProvider._

  def component(
      state: BoardUiLogic.State,
      observer: Observer[BoardUiLogic.Event]
  ): Element = {

    div(
      cls("w-[200px] flex flex-col bg-stone-800 p-3"),
      cls(bgColor),
      ExBoardForm.inputEl,
      "Board width",
      input(
        cls("text-gray-900"),
        typ("number"),
        controlled(
          value <-- boardWidthSignal(state).map(_.toString()),
          onInput.mapToValue.map(BoardUiLogic.BoardWidthCh) --> observer
        )
      ),
      p("Board height"),
      input(
        cls("text-gray-900"),
        typ("number"),
        controlled(
          value <-- boardHeightSignal(state).map(_.toString()),
          onInput.mapToValue.map(BoardUiLogic.BoardHeightCh) --> observer
        )
      )
    )
  }

}
