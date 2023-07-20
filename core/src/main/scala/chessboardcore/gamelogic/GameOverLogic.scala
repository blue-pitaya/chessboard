package chessboardcore.gamelogic

import chessboardcore.Model._
import chessboardcore.Vec2d

object GameOverLogic {
  import GameOverReasons._
  import GameOverState._

  def isGameOver(board: Board, turnFor: PieceColor): Option[GameOverState] = {
    val whiteKingOpt = board
      .pieces
      .find { case (pos, piece) =>
        piece == Piece(White, King)
      }
    val blackKingOpt = board
      .pieces
      .find { case (pos, piece) =>
        piece == Piece(Black, King)
      }

    (whiteKingOpt, blackKingOpt) match {
      case (None, None)    => Some(Draw(noKingsReason))
      case (Some(_), None) => Some(WinFor(White, noKingOf(Black)))
      case (None, Some(_)) => Some(WinFor(Black, noKingOf(White)))
      case (
            Some((whiteKingPos, whiteKingPiece)),
            Some((blackKingPos, blackKingPiece))
          ) => turnFor match {
          case White =>
            checkStalemateOrMate(board, whiteKingPos, whiteKingPiece.color)
          case Black =>
            checkStalemateOrMate(board, blackKingPos, blackKingPiece.color)
          case _ => None
        }
    }
  }

  private def checkStalemateOrMate(
      board: Board,
      kingPos: Vec2d,
      kingColor: PieceColor
  ): Option[GameOverState] = {
    val _canAnyPieceMove = canAnyPieceMove(board, kingColor)
    val _isKingChecked = MoveLogic.isKingChecked(board, kingPos, kingColor)

    (_canAnyPieceMove, _isKingChecked) match {
      case (false, false) => Some(Draw(staleMate))
      case (false, true) =>
        Some(WinFor(PieceColor.opposite(kingColor), regularWin))
      case _ => None
    }
  }

  private def canAnyPieceMove(board: Board, color: PieceColor): Boolean =
    MoveLogic.allPossibleMoves(board, col => col == color).isEmpty == false
}

object GameOverReasons {
  val noKingsReason = "Kingless kingdom is not kindgom, its anarchy!"
  def noKingOf(color: PieceColor) = color match {
    case Black => "Black king decided to not exists."
    case White => "White kingdom havent decided yet, who the king is."
  }
  val bothKingsMated =
    "Both kings are holding each other on gunpoint. People may chose now, peace or anarchy."
  val regularWin = "Just regular chess win. Nothing special."
  val staleMate = "Stalemate. King has hidden and refuse to face the world."
}
