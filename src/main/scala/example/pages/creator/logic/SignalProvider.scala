package example.pages.creator.logic

import com.raquo.laminar.api.L._

object SignalProvider {
  import BoardUiLogic._

  def boardHeightSignal(s: State): Signal[Int] = s.boardSize.signal.map(_.y)

  def boardWidthSignal(s: State): Signal[Int] = s.boardSize.signal.map(_.x)

}
