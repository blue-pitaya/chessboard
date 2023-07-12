package example.pages.game

import com.raquo.laminar.api.L._

case class State(fetchedServerState: Var[Option[ServerState]])
object State {
  def init = State(fetchedServerState = Var(None))
}

case class ServerState()
