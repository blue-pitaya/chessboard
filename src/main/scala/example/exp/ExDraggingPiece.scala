package example.exp

import cats.effect.IO
import com.raquo.laminar.api.L._
import example.game.Vec2d
import org.scalajs.dom

object ExDraggingPiece {
  import ExAppModel._

  def componentSignal(state: State, handler: Ev => IO[Unit]): Signal[Node] = {
    val dpsSignal = state.draggingPieceState.signal

    dpsSignal
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
    println(_centerPos)

    div(
      position.fixed,
      positionBinding(_centerPos),
      svg.svg(
        sizeBinding(tileSize),
        svg.image(sizeBinding(tileSize), svg.href(s.imgPath))
      )
    )
  }

  def pointerPosition(e: dom.PointerEvent): Vec2d =
    Vec2d(e.pageX.toInt, e.pageY.toInt)

  def positionBinding(centerPos: Vec2d) =
    Seq(left(toPx(centerPos.x)), top(toPx(centerPos.y)))

  def toPx(v: Int): String = v.toString() ++ "px"

  def centerPos(v: Vec2d, size: Int): Vec2d =
    Vec2d(v.x - (size / 2), v.y - (size / 2))

  def sizeBinding(tileSize: Int) =
    Seq(svg.width(tileSize.toString()), svg.height(tileSize.toString()))

}
