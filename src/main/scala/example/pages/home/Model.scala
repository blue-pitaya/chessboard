package example.pages.home

import com.raquo.laminar.api.L._

object Model {
  case class Entry()

  case class State(chessboardEntries: Var[List[Entry]])
}
