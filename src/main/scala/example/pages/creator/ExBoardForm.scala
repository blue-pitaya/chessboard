package example.pages.creator

import cats.effect.IO
import com.raquo.laminar.api.L._
import example.Utils
import example.Styles

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
      // button(
      //  Styles.btnCls,
      //  "Save board",
      //  onClick.mapToUnit -->
      //    commitEvObserver[Unit](handler, _ => SaveBoardRequested())
      // ),
      button(
        Styles.btnCls,
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
}
