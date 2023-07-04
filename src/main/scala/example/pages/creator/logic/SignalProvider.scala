package example.pages.creator.logic

import com.raquo.laminar.api.L._
import example.game.Vec2d

object SignalProvider {
  import BoardUiLogic._

  def boardHeightSignal(s: State): Signal[Int] = s.boardSize.signal.map(_.y)

  def boardWidthSignal(s: State): Signal[Int] = s.boardSize.signal.map(_.x)

  def tileCanvasPosSignal(pos: Vec2d, s: State): Signal[Vec2d] = s
    .boardSize
    .signal
    .map { boardSize =>
      _tileCanvasPos(pos, boardSize, s.canvasSize)
    }
}
