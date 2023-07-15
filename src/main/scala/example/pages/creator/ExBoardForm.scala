package example.pages.creator

import cats.effect.IO
import com.raquo.laminar.api.L._
import example.Utils

object ExBoardForm {
  import ExAppModel._

  def component(state: State, handler: Ev => IO[Unit]): Element = {
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
          onInput.mapToValue.map(v => BoardWidthChanged(v.toInt)) -->
            PiecePicker.catsRunObserver(handler)
        )
      ),
      "Board height",
      input(
        cls("text-gray-900"),
        typ("number"),
        controlled(
          value <-- heightSignal,
          onInput.mapToValue.map(v => BoardHeightChanged(v.toInt)) -->
            PiecePicker.catsRunObserver(handler)
        )
      ),
      button(
        cls(ButtonCls),
        "Save board",
        onClick.mapToUnit -->
          commitEvObserver[Unit](handler, _ => SaveBoardRequested())
      ),
      button(
        cls(ButtonCls),
        "Create game",
        onClick.mapToUnit -->
          commitEvObserver[Unit](
            handler,
            _ => CreateGameUsingCurrentBoardRequested()
          )
      )
    )
  }

  def commitEvObserver[A](
      handler: Ev => IO[Unit],
      mapFn: A => Ev
  ): Observer[A] = Observer[A] { v =>
    Utils.catsRun(handler)(mapFn(v))
  }

  private val ButtonCls =
    "text-gray-900 bg-white border border-gray-300 focus:outline-none hover:bg-gray-100 focus:ring-4 focus:ring-gray-200 font-medium rounded-lg text-sm px-5 py-2.5 mr-2 mb-2 dark:bg-gray-800 dark:text-white dark:border-gray-600 dark:hover:bg-gray-700 dark:hover:border-gray-600 dark:focus:ring-gray-700"

}
