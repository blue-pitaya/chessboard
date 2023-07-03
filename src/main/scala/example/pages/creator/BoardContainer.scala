package example.pages.creator

import com.raquo.laminar.api.L._
import dev.bluepitaya.laminardragging.Vec2f
import example.game.Vec2d
import example.pages.creator.logic.BoardUiLogic
import org.scalajs.dom
import dev.bluepitaya.laminardragging.Dragging
import example.pages.creator.logic.DraggingId

object BoardContainer {
  def component(
      s: BoardUiLogic.State,
      o: Observer[BoardUiLogic.Event],
      dm: Dragging.DraggingModule[DraggingId]
  ): Element = {
    val tileSize = BoardUiLogic.getTileSize(s)
    val tiles = s
      .tiles
      .signal
      .split(_.pos) { case (pos, tile, tileSignal) =>
        val canvasPos = BoardUiLogic.tileCanvasPos(s, pos)
        val bgColorSignal = tile
          .isHovered
          .signal
          .map(isHovered => BoardUiLogic.tileHexColor(pos, isHovered))

        tileComponent(canvasPos, tileSize, bgColorSignal)
      }
    val placedPieces = s
      .piecesOnBoard
      .signal
      .map(v => v.toList)
      .map { v =>
        v.map { case (pos, pob) =>
          val canvasPos = BoardUiLogic.tileCanvasPos(s, pos)
          val imgPath = BoardUiLogic.pieceImgPath(pob.piece)
          val draggingId = DraggingId.PieceOnBoardId(pos, pob.piece)
          val isVisibleSignal = pob.isVisible.signal

          placedPieceComponent(canvasPos, tileSize, imgPath, isVisibleSignal)
            .amend(
              dm.componentBindings(draggingId),
              dm.componentEvents(draggingId)
                .map(e =>
                  BoardUiLogic.PieceDragging(draggingId, pob.piece, e)
                ) --> o
            )
        }
      }

    svg.svg(
      svg.cls("min-w-[800px] h-[800px] bg-stone-800"),
      svg.g(children <-- tiles),
      svg.g(children <-- placedPieces),
      // TODO: remove?
      inContext { ctx =>
        onPointerMove.map(e =>
          BoardUiLogic.PointerMove(getRelativePosition(e, ctx.ref))
        ) --> o
      },
      onMountCallback { ctx =>
        o.onNext(BoardUiLogic.ContainerChanged(ctx.thisNode.ref))
      }
    )
  }

  def placedPieceComponent(
      pos: Vec2d,
      tileSize: Int,
      pieceImgPath: String,
      isVisibleSignal: Signal[Boolean]
  ): Element = {
    svg.image(
      svg.x(pos.x.toString()),
      svg.y(pos.y.toString()),
      svg.width(tileSize.toString()),
      svg.height(tileSize.toString()),
      svg.href <--
        isVisibleSignal.map {
          case true => pieceImgPath
          // TODO: quick hack
          case false => ""
        }
    )
  }

  /** Get dragging position relative to other element. */
  def getRelativePosition(
      e: dom.PointerEvent,
      container: dom.Element
  ): Vec2f = {
    val event = e
    val rect = container.getBoundingClientRect()
    val x = event.pageX - (rect.x + dom.window.pageXOffset)
    val y = event.pageY - (rect.y + dom.window.pageYOffset)

    Vec2f(x, y)
  }

  def tileComponent(
      pos: Vec2d,
      tileSize: Int,
      bgColorSignal: Signal[String]
  ): Element = {
    svg.rect(
      svg.x(pos.x.toString()),
      svg.y(pos.y.toString()),
      svg.width(tileSize.toString()),
      svg.height(tileSize.toString()),
      svg.fill <-- bgColorSignal
    )
  }
}
