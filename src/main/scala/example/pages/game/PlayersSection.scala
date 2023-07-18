package example.pages.game

import chessboardcore.Model.PlayerState._
import chessboardcore.Model._
import com.raquo.laminar.api.L._
import example.Styles

object PlayersSection {
  sealed trait Event
  case class PlayerSit(color: PieceColor) extends Event
  case class PlayerReady() extends Event

  case class Data(
      myPlayerId: String,
      whitePlayerState: Signal[PlayerState],
      blackPlayerState: Signal[PlayerState],
      gameStarted: Signal[Boolean],
      currentTurn: Signal[PieceColor]
  )

  def component(data: Data, handler: Observer[Event]): Element = {
    val _playerSection = (c: PieceColor, pls: PlayerState, gs: Boolean) =>
      playerSection(data.myPlayerId, c, pls, gs, handler)

    div(
      cls("flex flex-col bg-stone-800 gap-4 w-[200px]"),
      child <--
        data
          .whitePlayerState
          .combineWith(data.gameStarted)
          .map { case (plState, gs) =>
            _playerSection(White, plState, gs)
          },
      child <--
        data
          .blackPlayerState
          .combineWith(data.gameStarted)
          .map { case (plState, gs) =>
            _playerSection(Black, plState, gs)
          },
      child <-- data.currentTurn.map(currentTurnSection)
    )
  }

  def currentTurnSection(turn: PieceColor): Element = {
    val plLabel = playerLabel(turn)

    div(cls("flex flex-col"), div("Current turn:"), div(plLabel))
  }

  def playerSection(
      myPlayerId: String,
      color: PieceColor,
      plState: PlayerState,
      gameStarted: Boolean,
      handler: Observer[Event]
  ): Element = {
    val plLabel = playerLabel(color)

    val plActionSection =
      if (gameStarted) plActionSectionForStartedGame()
      else plActionSectionForNotStartedGame(myPlayerId, plState, color, handler)

    div(cls("flex flex-col gap-2 items-center"), plLabel, plActionSection)
  }

  private def playerLabel(color: PieceColor): String = color match {
    case Black => "Black player"
    case White => "White player"
  }

  def plActionSectionForStartedGame(): Element = {
    div("Playing...")
  }

  def plActionSectionForNotStartedGame(
      myPlayerId: String,
      plState: PlayerState,
      color: PieceColor,
      handler: Observer[Event]
  ): Element = {
    plState match {
      case Empty => button(
          Styles.btnCls,
          "Sit here",
          onClick.mapTo(PlayerSit(color)) --> handler
        )
      case Sitting(playerId) if playerId == myPlayerId =>
        button(
          Styles.btnCls,
          "I'm ready",
          onClick.mapTo(PlayerReady()) --> handler
        )
      case Sitting(playerId) => div("Waiting for player to be ready")
      case Ready(playerId) if playerId == myPlayerId =>
        div("You are ready to play!")
      case Ready(playerId) => div("Player is ready to play!")
    }
  }

}
