package example.pages.creator

import cats.effect.IO
import com.raquo.laminar.api.L._
import chessboardcore.Vec2d
import org.scalajs.dom

object ExDraggingPiece {
  import ExAppModel._

  def componentSignal(state: State, handler: Ev => IO[Unit]): Signal[Node] = {
    state
      .draggingPieceState
      .signal
      .withCurrentValueOf(state.boardSize)
      .map { case (sOpt, boardSize) =>
        sOpt match {
          case None    => emptyNode
          case Some(s) => component(boardSize, state.canvasSize, s)
        }
      }
  }

  def component(
      boardSize: Vec2d,
      canvasSize: Vec2d,
      s: DraggingPieceState
  ): Element = {
    val tileSize = ExBoard.tileSize(boardSize, canvasSize)
    val _centerPos = centerPos(pointerPosition(s.draggingEvent.e), tileSize)

    div(
      position.fixed,
      left(toPx(_centerPos.x)),
      top(toPx(_centerPos.y)),
      svg.svg(
        sizeBinding(tileSize),
        svg.image(sizeBinding(tileSize), svg.href(s.imgPath))
      )
    )
  }

  def centerPos(v: Vec2d, size: Int): Vec2d =
    Vec2d(v.x - (size / 2), v.y - (size / 2))

  def pointerPosition(e: dom.PointerEvent): Vec2d =
    Vec2d(e.pageX.toInt, e.pageY.toInt)

  def toPx(v: Int): String = v.toString() ++ "px"

  def sizeBinding(tileSize: Int) =
    Seq(svg.width(tileSize.toString()), svg.height(tileSize.toString()))
}
