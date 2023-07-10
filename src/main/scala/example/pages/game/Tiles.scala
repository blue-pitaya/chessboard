package example.pages.game

import com.raquo.laminar.api.L._
import example.game.GameState
import chessboardcore.Vec2d

object Tiles {
  def component(
      gameStateSignal: Signal[GameState],
      markedPositionsSignal: Signal[Set[Vec2d]],
      boardSettings: BoardSettings
  ) = {
    import example.Utils._

    val charStyle = cls("pointer-events-none select-none m-1")

    val markedTilesSignal = markedPositionsSignal
      .withCurrentValueOf(gameStateSignal)
      .map { case (s, gs) =>
        s.map { pos =>
            // TODO: DRY
            val posInPx = toRealPos(pos, gs.size, boardSettings.sizeInPx)
            val transformValue = transformStr(posInPx)
            val size = getTileSize(gs.size, boardSettings.sizeInPx)

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
            if (isDark) boardSettings.colorset.dark
            else boardSettings.colorset.light
          val tileSize = getTileSize(v.size, boardSettings.sizeInPx)
          val posInPx = toRealPos(pos, v.size, boardSettings.sizeInPx)
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
                span(),
                span(),
                span(),
                span(),
                span(),
                span(),
                span(),
                span(charStyle, fileMark.getOrElse[String](""))
              )
            )
          )
        }

    }

    svg.g(children <-- tilesSignal, children <-- markedTilesSignal)
  }
}
