package chessboardcore.gamelogic

import chessboardcore.Model._
import chessboardcore.Vec2d
import monocle.syntax.all._

object GameLogic {
  def createGame(board: Board): TrueGameState = {
    val firstTurnFor = White
    val gameOverState = GameOverLogic.isGameOver(board, firstTurnFor)

    TrueGameState(board, firstTurnFor, gameOverState)
  }

  def makeMove(
      from: Vec2d,
      to: Vec2d,
      playerColor: PieceColor,
      state: TrueGameState
  ): Either[String, TrueGameState] = {
    val gameIsNotOver = state.gameOver.isEmpty
    val itsPlayersTurn = playerColor == state.turn
    val moveIsPossible = MoveLogic.canMove(state.board, from, to)

    lazy val _commitMove = (s: TrueGameState) => commitMove(s, from, to)
    lazy val nextState = _commitMove andThen setGameOverIfStateTerminal

    for {
      _ <- trueOrErr(gameIsNotOver, "Game has already ended.")
      _ <- trueOrErr(itsPlayersTurn, "It's not your turn.")
      _ <- trueOrErr(moveIsPossible, "This move is illegal.")
    } yield (nextState(state))
  }

  private def setGameOverIfStateTerminal(state: TrueGameState): TrueGameState =
    GameOverLogic.isGameOver(state.board, state.turn) match {
      case Some(gameOver) => state.focus(_.gameOver).replace(Some(gameOver))
      case None           => state
    }

  private def commitMove(
      state: TrueGameState,
      from: Vec2d,
      to: Vec2d
  ): TrueGameState = state.board.pieces.get(from) match {
    case None => state
    case Some(piece) => state
        .focus(_.board.pieces)
        .modify(_.removed(from).updated(to, piece))
        .focus(_.turn)
        .modify(PieceColor.opposite(_))
  }

  private def trueOrErr(cond: Boolean, msg: String): Either[String, Unit] =
    Either.cond(cond, (), msg)
}
