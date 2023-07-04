package example.pages.creator

import com.raquo.laminar.api.L._
import dev.bluepitaya.laminardragging.Dragging
import dev.bluepitaya.laminardragging.Vec2f
import example.game.Vec2d
import example.pages.creator.logic.BoardUiLogic
import example.pages.creator.logic.DraggingId
import org.scalajs.dom

object BoardContainer {
  import BoardUiLogic._

  def component(
      s: State,
      o: Observer[Event],
      dm: Dragging.DraggingModule[DraggingId]
  ): Element = {
    val tilesSignal = s
      .boardSize
      .signal
      // TODO: clean
      .map(bs => (bs, tilesPositionMatrix(bs)))
      .map { case (bs, positions) =>
        val tileSize = _getTileSize(bs, s.canvasSize)
        positions.map { pos =>
          val canvasPos = _tileCanvasPos(pos, bs, s.canvasSize)
          val bgColor = tileHexColor(pos)

          tileComponent(canvasPos, tileSize, bgColor)
        }
      }

    // TODO: this is unsafe
    val placedPieces = s
      .piecesOnBoard
      .signal
      .map(v => v.toList)
      .combineWith(s.boardSize)
      .map { case (piecesOnBoard, boardSize) =>
        val tileSize = _getTileSize(boardSize, s.canvasSize)
        piecesOnBoard
          .filter { case (pos, _) =>
            isPieceOnBoard(pos, boardSize)
          }
          .map { case (pos, pieceOnBoard) =>
            val canvasPos = _tileCanvasPos(pos, boardSize, s.canvasSize)
            val imgPath = pieceImgPath(pieceOnBoard.piece)
            val draggingId = DraggingId.PieceOnBoardId(pos, pieceOnBoard.piece)
            val isVisibleSignal = pieceOnBoard.isVisible.signal

            placedPieceComponent(canvasPos, tileSize, imgPath, isVisibleSignal)
              .amend(
                dm.componentBindings(draggingId),
                dm.componentEvents(draggingId)
                  .map(e =>
                    BoardUiLogic
                      .PieceDragging(draggingId, pieceOnBoard.piece, e)
                  ) --> o
              )
          }
      }

    svg.svg(
      svg.cls("min-w-[800px] h-[800px] bg-stone-800"),
      svg.g(children <-- tilesSignal),
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

  def tileComponent(pos: Vec2d, tileSize: Int, bgColor: String): Element = {
    svg.rect(
      svg.x(pos.x.toString()),
      svg.y(pos.y.toString()),
      svg.width(tileSize.toString()),
      svg.height(tileSize.toString()),
      svg.fill(bgColor)
    )
  }
}
