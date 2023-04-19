package example.components

import com.raquo.laminar.api.L._
import org.scalajs.dom
import example.game.GameState
import xyz.bluepitaya.common.Vec2d
import example.TileColorset
import example.Renderer
import example.BoardDimens

trait BoardSettings {
  val sizeInPx: Vec2d
  val colorset: TileColorset
  // TODO: delete it
  val boardDimens: BoardDimens
}

object Tiles {
  def component(
      gameStateSignal: Signal[GameState],
      markedPositionsSignal: Signal[Set[Vec2d]],
      boardSettings: BoardSettings
  ) = {
    import example.Utils._

    val charStyle = cls("pointer-events-none select-none m-1")

    val markedTilesSignal = markedPositionsSignal.map { s =>
      s.map { pos =>
          // TODO: DRY
          val posInPx = Renderer.toRealPos(pos, boardSettings.boardDimens)
          val transformValue = transformStr(posInPx)
          val size = Renderer.getTileSize(boardSettings.boardDimens)

          svg.rect(
            svg.transform(transformValue),
            svg.width(toPx(size.x)),
            svg.height(toPx(size.y)),
            svg.fill("#623b69"),
            svg.fillOpacity("0.85")
          )
        }
        .toList
    }

    val tilesSignal = gameStateSignal.map { v =>
      Vec2d
        .zero
        .matrixUntil(v.size)
        .map { pos =>
          val isDark = (pos.x + pos.y) % 2 == 0
          val color =
            if (isDark) boardSettings.colorset.dark.value
            else boardSettings.colorset.light.value
          val tileSize = Renderer.getTileSize(boardSettings.boardDimens)
          val posInPx = Renderer.toRealPos(pos, boardSettings.boardDimens)
          val transformValue = transformStr(posInPx)

          val fileMark = pos match {
            case Vec2d(x, 0) => Some(('a' + x).toChar.toString())
            case _           => None
          }

          val rankMark = pos match {
            case Vec2d(0, y) => Some((1 + y).toString())
            case _           => None
          }

          svg.g(
            svg.rect(
              svg.transform(transformValue),
              svg.width(tileSize.x.toString()),
              svg.height(tileSize.y.toString()),
              svg.fill(color)
            ),
            // TODO: marks can be somwhere else
            svg.foreignObject(
              svg.transform(transformValue),
              svg.width(tileSize.x.toString()),
              svg.height(tileSize.y.toString()),
              div(
                cls("w-full h-full text-black grid"),
                styleProp("grid-template-columns") := "auto 1fr auto",
                styleProp("grid-template-rows") := "auto 1fr auto",
                // a lot of spans
                span(charStyle, rankMark.getOrElse[String]("")),
                // TODO: do i nedd charStyle on empty tiles?
                span(charStyle),
                span(charStyle),
                span(charStyle),
                span(charStyle),
                span(charStyle),
                span(charStyle),
                span(charStyle),
                span(charStyle, fileMark.getOrElse[String](""))
              )
            )
          )
        }

    }

    svg.g(children <-- tilesSignal, children <-- markedTilesSignal)
  }
}
