package example.pages.game

import com.raquo.laminar.api.L._
import chessboardcore.Model
import chessboardcore.Model.PlayerState.Empty
import chessboardcore.Model.PlayerState.Ready
import chessboardcore.Model.PlayerState.Sitting

object PlayersSection {
  case class Data(
      whitePlayerState: Var[Model.PlayerState],
      blackPlayerState: Var[Model.PlayerState]
  )

  def component(data: Data): Element = {
    div(
      cls("flex flex-col bg-stone-800"),
      div("player 1 section"),
      div("player 2 section")
    )
  }

  def playerComponent(state: Model.PlayerState): Element = {
    state match {
      case Empty   => button("Sit here")
      case Ready   => button("I'm ready")
      case Sitting => div("you are playing")
    }
  }
}
