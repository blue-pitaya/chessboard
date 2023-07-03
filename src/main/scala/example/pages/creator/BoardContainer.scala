package example.pages.creator

import com.raquo.laminar.api.L._
import dev.bluepitaya.laminardragging.Vec2f
import example.game.Vec2d
import example.pages.creator.logic.BoardUiLogic
import org.scalajs.dom

object BoardContainer {
  def component(
      s: BoardUiLogic.State,
      o: Observer[BoardUiLogic.Event]
  ): Element = {
    val tileSize = BoardUiLogic.getTileSize(s)
    val tiles = s
      .tiles
      .signal
      .split(_.pos) { case (pos, tile, tileSignal) =>
        val canvasPos = BoardUiLogic.getTilePos(s, pos)
        val bgColorSignal = tile
          .isHovered
          .signal
          .map(isHovered => BoardUiLogic.tileHexColor(pos, isHovered))
        tileComponent(canvasPos, tileSize, bgColorSignal)
      }
    val placedPieces = s
      .placedPieces
      .signal
      .map(v => v.toList)
      .split(_._1) { case (pos, (_, piece), _) =>
        val canvasPos = BoardUiLogic.getTilePos(s, pos)
        val imgPath = BoardUiLogic.pieceImgPath(piece)
        placedPieceComponent(canvasPos, tileSize, imgPath)
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
      pieceImgPath: String
  ): Element = {
    svg.image(
      svg.x(pos.x.toString()),
      svg.y(pos.y.toString()),
      svg.width(tileSize.toString()),
      svg.height(tileSize.toString()),
      svg.href(pieceImgPath)
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
