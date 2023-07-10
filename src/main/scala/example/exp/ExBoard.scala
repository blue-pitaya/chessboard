package example.exp

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.raquo.laminar.api.L._
import org.scalajs.dom
import example.game.Vec2d

object ExBoard {
  import ExAppModel._
  import ExApp.DM

  type BoardSize = Vec2d
  type BoardPos = Vec2d

  def component(state: State, handler: Ev => IO[Unit]): Element = {
    val canvasSize = state.canvasSize

    val _tileSize = (bs: BoardSize) => tileSize(bs, canvasSize)
    val _tileCanvasPos =
      (bs: BoardSize) => (pos: BoardPos) => tileCanvasPos(canvasSize, bs, pos)
    val _tileComponent = (boardSize: BoardSize) =>
      (pos: BoardPos) => {
        val tileSize = _tileSize(boardSize)
        tileComponent(_tileCanvasPos(boardSize), tileSize, pos)
      }
    val _tileComponents =
      (bs: BoardSize) => tileComponents(_tileComponent(bs), bs)
    val _tilesSignal = tilesSignal(_tileComponents, state.boardSize.signal)
    val _placedPieceDraggingBindings =
      (pos: BoardPos) => placedPieceDraggingBindings(pos, state.dm, handler)

    val _placedPiecesSignal = placedPiecesSignal(
      state.boardSize.signal,
      state.placedPieces.signal,
      _tileSize,
      _tileCanvasPos,
      _placedPieceDraggingBindings
    )

    svg.svg(
      // TODO: should take canvasSize, but there is no styleProp for svg
      // and interpolation string for tailwind cls is broken
      svg.cls("min-w-[800px] h-[800px] bg-stone-800"),
      svg.g(children <-- _tilesSignal),
      svg.g(children <-- _placedPiecesSignal),
      onMountCallback(onMountCallBkEffect(_, handler))
    )
  }

  def placedPieceDraggingBindings(
      fromPos: Vec2d,
      dm: DM[PieceDraggingId],
      handler: Ev => IO[Unit]
  ): Seq[Binder.Base] = {
    val draggingId = PlacedPieceDraggingId(fromPos)
    dm.componentBindings(draggingId) ++
      Seq(
        dm.componentEvents(draggingId)
          .map(e => PlacedPieceDragging(e, fromPos)) -->
          ExApp.catsRunObserver(handler)
      )
  }

  def placedPiecesSignal(
      boardSizeSignal: Signal[Vec2d],
      placedPiecesSignal: Signal[PlacedPieces],
      tileSize: BoardSize => Int,
      canvasPos: BoardSize => BoardPos => Vec2d,
      draggingBindings: BoardPos => Seq[Binder.Base]
  ): Signal[List[Element]] = {
    placedPiecesSignal
      .combineWith(boardSizeSignal)
      .map { case (placedPieces, boardSize) =>
        placedPiecesOnBoard(placedPieces, boardSize).map { case (pos, piece) =>
          val imgPath = ExApp.pieceImgPath(piece.color, piece.piece)
          placedPieceComponent(
            canvasPos(boardSize)(pos),
            tileSize(boardSize),
            imgPath,
            piece.isVisible.signal,
            draggingBindings(pos)
          )
        }
      }
  }

  def placedPiecesOnBoard(
      placedPieces: PlacedPieces,
      boardSize: Vec2d
  ): List[(Vec2d, ColoredPiece)] = {
    placedPieces
      .toList
      .collect {
        case (pos, piece) if isPosOnBoard(pos, boardSize) => (pos, piece)
      }
  }

  def isPosOnBoard(pos: Vec2d, boardSize: Vec2d): Boolean =
    isBetween(pos, Vec2d.zero, boardSize)

  def isBetween(v: Vec2d, b1: Vec2d, b2: Vec2d): Boolean = v.x >= b1.x &&
    v.y >= b1.y && v.x < b2.x && v.y < b2.y

  def placedPieceComponent(
      pos: Vec2d,
      tileSize: Int,
      pieceImgPath: String,
      isVisibleSignal: Signal[Boolean],
      draggingBindings: Seq[Binder.Base]
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
        },
      draggingBindings
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

  def tileCanvasPos(canvasSize: Vec2d, boardSize: Vec2d, pos: Vec2d): Vec2d = {
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

  def onMountCallBkEffect(
      ctx: MountContext[Element],
      handler: Ev => IO[Unit]
  ): Unit = catsRun(handler)(BoardContainerRefChanged(ctx.thisNode.ref))

  def catsRun[A](f: A => IO[Unit]): A => Unit = { e =>
    f(e).unsafeRunAsync { cb =>
      cb match {
        case Left(err)    => dom.console.error(err.toString())
        case Right(value) => ()
      }
    }
  }
}
