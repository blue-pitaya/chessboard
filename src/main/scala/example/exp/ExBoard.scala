package example.exp

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.raquo.laminar.api.L._
import org.scalajs.dom
import example.game.Vec2d

object ExBoard {
  import ExAppModel._

  def component(state: State, handler: Ev => IO[Unit]): Element = {
    val canvasSize = state.canvasSize

    val _tileSize = (boardSize: Vec2d) => tileSize(boardSize, canvasSize)
    val _tileCanvasPos = (boardSize: Vec2d) =>
      (pos: Vec2d) => tileCanvasPos(canvasSize, boardSize)(pos)
    val _tileComponent = (boardSize: Vec2d) =>
      (pos: Vec2d) => {
        val tileSize = _tileSize(boardSize)
        tileComponent(_tileCanvasPos(boardSize), tileSize, pos)
      }
    val _tileComponents =
      (boardSize: Vec2d) => tileComponents(_tileComponent(boardSize), boardSize)
    val _tilesSignal = tilesSignal(_tileComponents, state.boardSize.signal)

    svg.svg(
      // TODO: should take canvasSize, but there is no styleProp for svg
      // and interpolation string for tailwind cls is broken
      svg.cls("min-w-[800px] h-[800px] bg-stone-800"),
      svg.g(children <-- _tilesSignal),
      // svg.g(children <-- placedPieces),
      onMountCallback(ctx => omc(ctx, handler))
    )
  }

  def tilesSignal(
      tileComponents: Vec2d => List[Element],
      size: Signal[Vec2d]
  ): Signal[List[Element]] = size.map(tileComponents)

  def tileComponents(
      tileComponent: Vec2d => Element,
      boardSize: Vec2d
  ): List[Element] = {
    val tileLogicPositions = vec2dMatrix(boardSize)
    tileLogicPositions.map(tileComponent)
  }

  def vec2dMatrix(size: Vec2d): List[Vec2d] = (0 until size.x)
    .map { x =>
      (0 until size.y).map { y =>
        Vec2d(x, y)
      }
    }
    .toList
    .flatten

  def tileComponent(
      tileCanvasPos: Vec2d => Vec2d,
      tileSize: Int,
      logicPos: Vec2d
  ): Element = {
    val pos = tileCanvasPos(logicPos)
    val size = tileSize
    val bgColor = tileColor(logicPos)

    svg.rect(
      svg.x(pos.x.toString()),
      svg.y(pos.y.toString()),
      svg.width(size.toString()),
      svg.height(size.toString()),
      svg.fill(bgColor)
    )
  }

  def tileCanvasPos(canvasSize: Vec2d, boardSize: Vec2d)(pos: Vec2d): Vec2d = {
    val _tileSize = tileSize(boardSize, canvasSize)
    val _boardOffset = boardOffset(_tileSize, boardSize, canvasSize)
    val x = pos.x * _tileSize
    val y = (canvasSize.y - _tileSize) - (pos.y * _tileSize)

    Vec2d(_boardOffset.x + x, y - _boardOffset.y)
  }

  def boardOffset(tileSize: Int, boardSize: Vec2d, canvasSize: Vec2d): Vec2d =
    (canvasSize - (boardSize * tileSize)) / 2

  def tileSize(boardSize: Vec2d, canvasSize: Vec2d): Int = {
    val maxSize = 100
    val x = canvasSize.x / boardSize.x
    val y = canvasSize.y / boardSize.y

    Math.min(Math.min(maxSize, x), Math.min(maxSize, y))
  }

  def tileColor(pos: Vec2d): String = {
    val blackTileColor = "#b58863"
    val whiteTileColor = "#f0d9b5"

    if ((pos.x + pos.y) % 2 == 0) blackTileColor
    else whiteTileColor
  }

  // TODO: change name
  def omc(ctx: MountContext[Element], handler: Ev => IO[Unit]): Unit =
    catsRun(handler)(BoardContainerRefChanged(ctx.thisNode.ref))

  def catsRun[A](f: A => IO[Unit]): A => Unit = { e =>
    f(e).unsafeRunAsync { cb =>
      cb match {
        case Left(err)    => dom.console.error(err.toString())
        case Right(value) => ()
      }
    }
  }
}
