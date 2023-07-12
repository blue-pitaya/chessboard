package example.pages.game

import com.raquo.laminar.api.L._
import cats.effect.IO

object GamePage {
  def component(id: String): Element = {
    val state = State.init

    div(child <-- innerComponentSignal(state))
  }

  def innerComponentSignal(state: State): Signal[Element] = state
    .fetchedServerState
    .signal
    .map {
      case Some(s) => loadedComponent(state, s)
      case None    => notLoadedComponent()
    }

  def notLoadedComponent(): Element = {
    div("Fetching game from server...")
  }

  def loadedComponent(state: State, serverState: ServerState): Element = {
    div(p(state.toString()))
  }

  def loadFromServer(id: String, state: State): IO[Unit] = ???
}
