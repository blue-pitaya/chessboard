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
import example.components.BoardSettings
import example.game.GameState

case class PieceObj(id: String, position: Vec2d, piece: models.Piece)

object PieceObj {
  def fromPieces(pieces: Map[Vec2d, models.Piece]): List[PieceObj] = pieces
    .zipWithIndex
    .map { case ((pos, piece), idx) =>
      PieceObj(idx.toString(), pos, piece)
    }
    .toList
}

object Piece {
  def component(
      pieceObj: PieceObj,
      gameState: GameState,
      draggingModule: Dragging.DraggingModule[String],
      highlightTiles: Set[Vec2d] => Unit,
      boardSettings: BoardSettings,
      // TODO: makeMove
      updateGameState: GameState => Unit,
      movePieceToFront: String => Unit
  ) = {
    val id = pieceObj.id
    val position = pieceObj.position
    val piece = pieceObj.piece

    val renderPosition =
      Var[Vec2d](Renderer.toRealPos(position, boardSettings.boardDimens))

    val size = Renderer.getTileSize(boardSettings.boardDimens)
    val imagePath = resolveImagePath(piece)

    import DragEventKind._
    val draggingObserver = Observer[
      Either[RelativeDragging.ContainerNotFound, RelativeDragging.Event]
    ] {
      case Left(error) => dom
          .console
          .error(s"Dragging error on ${error.kind}. No parent container found.")

      case Right(event) => event match {
          case RelativeDragging.Event(_, Start, pos) =>
            val possibleMoves = PossibleMoves
              .getMoveTiles(position, gameState)
              .map(_._1)
              .toSet
            highlightTiles(possibleMoves)
            renderPosition.set(pos.toVec2d - (size / 2))
            movePieceToFront(id)

          case RelativeDragging.Event(_, End, pos) =>
            val possibleMoves = PossibleMoves
              .getMoveTiles(position, gameState)
              .map(_._1)
              .toSet
            renderPosition
              .set(Renderer.toRealPos(position, boardSettings.boardDimens))
            highlightTiles(Set())
            val logicPosition = Renderer
              .toLogicPostion(pos.toVec2d, boardSettings.boardDimens)

            if (possibleMoves.contains(logicPosition)) {
              val x = GameLogic.makeMove(position, logicPosition, gameState)
              x.foreach(updateGameState)
            }

          case RelativeDragging.Event(_, _, pos) =>
            renderPosition.set(pos.toVec2d - (size / 2))
        }
    }

    val draggingId = s"piece-$id"

    import Utils._
    svg.image(
      svg.transform <-- renderPosition.signal.map(transformStr),
      svg.xlinkHref(imagePath),
      svg.width(toPx(size.x)),
      svg.height(toPx(size.y)),
      draggingModule.componentBindings(draggingId),
      draggingModule
        .componentEvents(draggingId)
        .map(RelativeDragging.getMappingDynamic(getContainer)) -->
        draggingObserver
    )
  }

  private def getContainer(el: dom.Element): Boolean =
    Option(el.getAttribute("id")).map(_ == Board.svgElementId).getOrElse(false)

  private def resolveImagePath(piece: models.Piece) = {
    val color = piece.color.toString().toLowerCase()
    val kind = piece.kind.toString().toLowerCase()
    val filename = s"$color-${kind}.png"

    s"/pieces/$filename"
  }
}
