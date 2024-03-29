package example.pages.game

import chessboardcore.HttpModel._
import chessboardcore.Model._
import chessboardcore.Vec2d
import chessboardcore.gamelogic.MoveLogic
import com.raquo.laminar.api.L._
import dev.bluepitaya.laminardragging.DragEventKind
import dev.bluepitaya.laminardragging.Dragging
import example.AppModel
import example.HttpClient
import example.Utils
import example.components.BoardComponent
import example.components.BoardComponent.ElementRefChanged
import example.components.BoardComponent.PieceDragging
import example.components.DraggingPiece
import example.components.Logic
import io.circe.generic.auto._
import io.laminext.websocket.WebSocket
import io.laminext.websocket.circe._

//TODO: clean, should be named GamePageLogic

object GameService {
  import GamePage.State

  case class Module(state: State, bindings: Seq[Binder.Base])

  def wire(
      gameId: String,
      events: EventStream[GamePage.Event],
      plSectionEvents: EventStream[PlayersSectionComponent.Event],
      boardCompEvents: EventStream[BoardComponent.Event]
  ): Module = {
    // ignore potential error, it's unlikely to happen
    // and also what if it happen? some kid wouldn't be able
    // to play some custom chess or whatever
    val playerId = Utils.catsUnsafeRunSync(PlayerService.createOfLoadPlayerId())
    val token = Utils.catsUnsafeRunSync(PlayerService.createOrLoadToken())
    val state = createState(playerId, token)

    val ws = WebSocket
      .url(HttpClient.gameWebSockerUrl(gameId))
      .json[GameEvent_Out, WsInputMessage]
      .build()

    val bindings = Seq(
      ws.received.-->(Observer[GameEvent_Out](e => handleWsEvent(e, state))),
      events
        .-->(Observer[GamePage.Event](e => handleEvent(e, state, ws.sendOne))),
      plSectionEvents.-->(
        Observer[PlayersSectionComponent.Event](e =>
          handlePlSectionEvent(e, state, ws.sendOne)
        )
      ),
      boardCompEvents.-->(
        Observer[BoardComponent.Event](e =>
          handleBoardComponentEvent(e, state, ws.sendOne)
        )
      ),
      ws.connect
    )

    Module(state, bindings)
  }

  def shouldBoardBeFlipped(
      playerId: String,
      players: Map[PieceColor, PlayerState],
      gameStarted: Boolean
  ): Boolean = {
    (players.get(White), players.get(Black)) match {
      case (Some(PlayerState(whiteId, _)), Some(PlayerState(blackId, _)))
          if blackId != whiteId && blackId == playerId && gameStarted => true
      case _ => false
    }
  }

  private def createState(playerId: String, token: String) = State(
    Var(TrueGameState.empty),
    Var(Map()),
    Var(false),
    playerId,
    Var(None),
    Var(None),
    Var(Map()),
    Var(Set()),
    Var(None),
    token
  )

  private def handleWsEvent(e: GameEvent_Out, state: GamePage.State): Unit = {
    state.gameState.set(e.gameState)
    state.pieces.set(pieces(e.gameState.board))
    state.gameStarted.set(e.gameStarted)
    state.players.set(e.players)
    state.msgFromApi.set(e.msg)
  }

  private def pieces(board: Board): Map[Vec2d, BoardComponent.PieceUiModel] =
    board
      .pieces
      .map { case (pos, piece) =>
        (pos, createPieceModel(piece))
      }

  private def createPieceModel(piece: Piece): BoardComponent.PieceUiModel =
    BoardComponent.PieceUiModel(piece, Var(true))

  private def handleEvent(
      e: GamePage.Event,
      state: GamePage.State,
      sendWsEvent: WsInputMessage => Unit
  ): Unit = e match {
    case GamePage.RequestGameState() =>
      sendWsEvent(WsInputMessage(state.token, GetGameState()))
  }

  private def handlePlSectionEvent(
      e: PlayersSectionComponent.Event,
      state: GamePage.State,
      sendWsEvent: WsInputMessage => Unit
  ): Unit = e match {
    case PlayersSectionComponent.PlayerSit(color) =>
      sendWsEvent(WsInputMessage(state.token, PlayerSit(state.playerId, color)))
    case PlayersSectionComponent.PlayerReady(color) => sendWsEvent(
        WsInputMessage(state.token, PlayerReady(state.playerId, color))
      )
  }

  private def handleBoardComponentEvent(
      e: BoardComponent.Event,
      state: GamePage.State,
      sendWsEvent: WsInputMessage => Unit
  ): Unit = {
    e match {
      case ElementRefChanged(v) => state.boardComponentRef.set(Some(v))
      case PieceDragging(e, fromPos) =>
        val pieceModelOpt = state.pieces.now().get(fromPos)
        val gameStarted = state.gameStarted.now()
        val myPlayerId = state.playerId
        val currentTurn = state.gameState.now().turn

        val isMyPiece = (col: PieceColor) =>
          playerId(state, col).map(_ == myPlayerId).getOrElse(false)

        (pieceModelOpt, gameStarted) match {
          case (Some(pieceModel), true) if isMyPiece(pieceModel.piece.color) =>
            val _movePiece =
              (to: Vec2d) => movePiece(state, fromPos, to, pieceModel)
            val sendMoveEvent = (toPos: Vec2d) =>
              sendWsEvent(
                WsInputMessage(
                  state.token,
                  Move(myPlayerId, fromPos, toPos, pieceModel.piece.color)
                )
              )

            handlePieceDragging(
              e,
              fromPos,
              pieceModel,
              state,
              sendMoveEvent,
              _movePiece
            )
          case _ => ()
        }
    }
  }

  // TODO: dup?
  private def playerId(
      state: GamePage.State,
      color: PieceColor
  ): Option[String] = state.players.now().get(color).map(_.id)

  private def handlePieceDragging(
      e: Dragging.Event,
      pos: Vec2d,
      pieceModel: BoardComponent.PieceUiModel,
      state: GamePage.State,
      sendMoveEvent: Vec2d => Unit,
      movePiece: Vec2d => Unit
  ): Unit = {
    lazy val board = state.gameState.now().board
    val _updatePieceDraggingState = () =>
      updatePieceDraggingState(state, e, Utils.pieceImgPath(pieceModel.piece))

    e.kind match {
      case DragEventKind.Start =>
        pieceModel.isVisible.set(false)
        _updatePieceDraggingState()
        val possibleMoves = MoveLogic.possibleMoves(board, pos)
        state.highlightedTiles.set(possibleMoves.toSet)
      case DragEventKind.Move => _updatePieceDraggingState()
      case DragEventKind.End =>
        state.highlightedTiles.set(Set())
        pieceModel.isVisible.set(true)
        state.draggingPieceState.set(None)
        val toPosOpt = tileLogicPos(state, e)
        toPosOpt.foreach { toPos =>
          movePiece(toPos)
          sendMoveEvent(toPos)
        }
    }
  }

  // TODO: dup with EvHandler
  private def movePiece(
      state: GamePage.State,
      fromPos: Vec2d,
      toPos: Vec2d,
      pieceUiModel: BoardComponent.PieceUiModel
  ): Unit = {
    state.pieces.update(v => v.removed(fromPos))
    state.pieces.update(v => v.updated(toPos, pieceUiModel))
  }

  // TODO: dup with EvHandler
  private def tileLogicPos(
      state: GamePage.State,
      e: Dragging.Event
  ): Option[Vec2d] = {
    val boardSize = state.gameState.now().board.size
    val boardFlipped = shouldBoardBeFlipped(
      state.playerId,
      state.players.now(),
      state.gameStarted.now()
    )
    val elementRefOpt = state.boardComponentRef.now()

    elementRefOpt.flatMap { elementRef =>
      val canvasSize = AppModel.DefaultBoardCanvasSize
      val canvasPos = Utils.getRelativePosition(e.e, elementRef)
      Logic.tileLogicPos(boardSize, canvasSize, canvasPos, boardFlipped)
    }
  }

  private def updatePieceDraggingState(
      state: GamePage.State,
      draggingEvent: Dragging.Event,
      imgPath: String
  ): Unit = {
    val dps = DraggingPiece.DraggingPieceState(imgPath, draggingEvent)

    state.draggingPieceState.set(Some(dps))
  }

}
