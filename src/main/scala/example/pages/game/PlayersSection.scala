package example.pages.game

import chessboardcore.Model.PlayerState._
import chessboardcore.Model._
import com.raquo.laminar.api.L._
import example.Styles

object PlayersSection {
  sealed trait Event
  case class PlayerSit(color: PieceColor) extends Event

  case class Data(
      whitePlayerState: Signal[PlayerState],
      blackPlayerState: Signal[PlayerState]
  )

  def component(data: Data, handler: Observer[Event]): Element = {
    val _playerSection =
      (c: PieceColor, pls: PlayerState) => playerSection(c, pls, handler)

    div(
      cls("flex flex-col bg-stone-800 gap-4 w-[200px]"),
      child <-- data.whitePlayerState.map(v => _playerSection(White, v)),
      child <-- data.blackPlayerState.map(v => _playerSection(Black, v))
    )
  }

  def playerSection(
      color: PieceColor,
      plState: PlayerState,
      handler: Observer[Event]
  ): Element = {
    val plLabel = color match {
      case Black => "Black player"
      case White => "White player"
    }

    val plActionSection = plState match {
      case Empty => button(
          Styles.btnCls,
          "Sit here",
          onClick.mapTo(PlayerSit(color)) --> handler
        )
      case Ready   => button("I'm ready")
      case Sitting => div("you are playing")
    }

    div(cls("flex flex-col gap-2 items-center"), plLabel, plActionSection)
  }

}
