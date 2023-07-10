package example.pages.creator

import com.raquo.laminar.api.L._
import com.raquo.laminar.codecs.StringAsIsCodec
import cats.effect.IO

object ExBoardForm {
  import ExAppModel._

  def component(state: State, handler: Ev => IO[Unit]): Element = {
    val widthSignal = state.boardSize.signal.map(_.x.toString())
    val heightSignal = state.boardSize.signal.map(_.y.toString())

    div(
      cls("w-[200px] flex flex-col bg-stone-800 p-3"),
      "Board width",
      input(
        cls("text-gray-900"),
        typ("number"),
        controlled(
          value <-- widthSignal,
          onInput.mapToValue.map(v => BoardWidthChanged(v.toInt)) -->
            ExApp.catsRunObserver(handler)
        )
      ),
      p("Board height"),
      input(
        cls("text-gray-900"),
        typ("number"),
        controlled(
          value <-- heightSignal,
          onInput.mapToValue.map(v => BoardHeightChanged(v.toInt)) -->
            ExApp.catsRunObserver(handler)
        )
      )
    )
  }

  private def inputEl = {
    val v = Var("")

    div(
      cls("relative mb-3"),
      htmlAttr("data-te-input-wrapper-init", StringAsIsCodec) := "",
      input(
        typ("number"),
        cls(
          "peer block min-h-[auto] w-full rounded border-0 bg-transparent px-3 py-[0.32rem] leading-[1.6] outline-none transition-all duration-200 ease-linear focus:placeholder:opacity-100 peer-focus:text-primary data-[te-input-state-active]:placeholder:opacity-100 motion-reduce:transition-none dark:text-neutral-200 dark:placeholder:text-neutral-200 dark:peer-focus:text-primary [&:not([data-te-input-placeholder-active])]:placeholder:opacity-0"
        ),
        idAttr("pl"),
        placeholder("Example label"),
        controlled(value <-- v, onInput.mapToValue --> v)
      ),
      label(
        forId("pl"),
        cls(
          "pointer-events-none absolute left-3 top-0 mb-0 max-w-[90%] origin-[0_0] truncate pt-[0.37rem] leading-[1.6] text-neutral-500 transition-all duration-200 ease-out peer-focus:-translate-y-[0.9rem] peer-focus:scale-[0.8] peer-focus:text-primary peer-data-[te-input-state-active]:-translate-y-[0.9rem] peer-data-[te-input-state-active]:scale-[0.8] motion-reduce:transition-none dark:text-neutral-200 dark:peer-focus:text-primary"
        ),
        "Number input"
      )
    )
  }

//  <div class="relative mb-3" data-te-input-wrapper-init>
//  <input
//    type="number"
//    class=""
//    id="exampleFormControlInputNumber"
//    placeholder="Example label" />
//  <label
//    for="exampleFormControlInputNumber"
//    class=""
//    >Number input
//  </label>
//</div>
}
