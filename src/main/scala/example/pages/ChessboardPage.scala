package example.pages

import com.raquo.laminar.api.L._
import example.components.Board

object ChessboardPage {
  private val headerText = """
    |Simple chessborad, with implementes chess rules (without turn restrictions).
    |Todo: promoting pawn, rewriting code to Cats Effect, some fancy featureas
    |like playing actual game xd.
    """.stripMargin.trim()

  def component() = {
    div(
      p(headerText),
      div(
        cls("w-full h-full flex flex-row items-center justify-center"),
        Board.component()
      )
    )
  }
}
