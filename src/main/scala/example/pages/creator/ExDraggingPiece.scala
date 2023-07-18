package example.pages.creator

import chessboardcore.Vec2d
import com.raquo.laminar.api.L._
import org.scalajs.dom

object ExDraggingPiece {
  import ExAppModel._

  sealed trait Event

  case class Data(
      draggingPieceState: Signal[Option[DraggingPieceState]],
      boardSize: Signal[Vec2d],
      canvasSize: Vec2d
  )

  def componentSignal(data: Data): Signal[Node] = {
    data
      .draggingPieceState
      .signal
      .withCurrentValueOf(data.boardSize)
      .map { case (sOpt, boardSize) =>
        sOpt match {
          case None    => emptyNode
          case Some(s) => component(boardSize, data.canvasSize, s)
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
