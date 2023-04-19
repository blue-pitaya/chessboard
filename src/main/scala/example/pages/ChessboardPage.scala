package example.pages

import com.raquo.laminar.api.L._
import example.Styles
import org.scalajs.dom
import example.Board

object ChessboardPage {
  private val headerText = """
    |Simple chessborad, with implementes chess rules (without turn restrictions).
    |Todo: promoting pawn, rewriting code to Cats Effect, some fancy featureas
    |like playing actual game xd.
    """.stripMargin.trim()

  def component() = {
    div(p(headerText), div(Styles.centerElement, Board.component()))
  }
}
