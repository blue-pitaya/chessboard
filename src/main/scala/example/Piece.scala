package example

import com.raquo.laminar.api.L._
import example.game.GameMove
import example.game.PossibleMoves
import example.models
import org.scalajs.dom
import xyz.bluepitaya.common.Vec2d
import xyz.bluepitaya.common.Vec2f
import xyz.bluepitaya.laminardragging.DeltaDragging
import xyz.bluepitaya.laminardragging.DragEventKind
import xyz.bluepitaya.laminardragging.Dragging
import xyz.bluepitaya.laminardragging.RelativeDragging

import scala.util.Random
import example.game.GameLogic

object Piece {
  def component(
      id: String,
      position: Vec2d,
      piece: models.Piece,
      boardState: Board.State,
      container: dom.Element
  ) = {
    val renderPosition =
      Var[Vec2d](Renderer.toRealPos(position, boardState.boardDimens))

    val size = Renderer.getTileSize(boardState.boardDimens)
    val imagePath = resolveImagePath(piece)
    val draggingModule = boardState.draggingModule

    import DragEventKind._
    val draggingObserver = Observer[RelativeDragging.Event] { e =>
      e match {
        case RelativeDragging.Event(_, Start, pos) =>
          val possibleMoves = PossibleMoves
            .getMoveTiles(position, boardState.gameState.now())
            .map(_._1)
            .toSet
          boardState.highlightedTiles.set(possibleMoves)
          renderPosition.set(pos.toVec2d - (size / 2))
        case RelativeDragging.Event(_, End, pos) =>
          val possibleMoves = PossibleMoves
            .getMoveTiles(position, boardState.gameState.now())
            .map(_._1)
            .toSet
          renderPosition
            .set(Renderer.toRealPos(position, boardState.boardDimens))
          boardState.highlightedTiles.set(Set())
          val logicPosition = Renderer
            .toLogicPostion(pos.toVec2d, boardState.boardDimens)

          if (possibleMoves.contains(logicPosition)) {
            val x = GameLogic
              .makeMove(position, logicPosition, boardState.gameState.now())
            x.foreach(boardState.gameState.set)
          }

        case RelativeDragging.Event(_, _, pos) => renderPosition
            .set(pos.toVec2d - (size / 2))
      }
    }

    import Utils._
    svg.image(
      svg.transform <-- renderPosition.signal.map(transformStr),
      svg.xlinkHref(imagePath),
      svg.width(toPx(size.x)),
      svg.height(toPx(size.y)),
      draggingModule.componentBindings(id),
      draggingModule
        .componentEvents(id)
        .map(RelativeDragging.getMapping(container)) --> draggingObserver
    )
  }

  private def resolveImagePath(piece: models.Piece) = {
    val color = piece.color.toString().toLowerCase()
    val kind = piece.kind.toString().toLowerCase()
    val filename = s"$color-${kind}.png"

    s"/pieces/$filename"
  }
}
