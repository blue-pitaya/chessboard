package example.game

import example.GameState
import example.Move
import example.models._
import xyz.bluepitaya.common.Vec2d
import example.GameMove
import example.CastlingMove

object GameLogic {
  // returns None if move is not en passant, position of piece to kill otherwise
  private def enPassanedPiecePos(
      move: Move,
      state: GameState
  ): Option[Vec2d] = {
    val isEnPassant = move.piece.kind == Pawn &&
      PossibleMoves
        .getEnPassantMoves(move.from, move.piece.color, state.lastMove)
        .contains(move.to)
    if (isEnPassant) state
      .lastMove
      .flatMap {
        case m: Move => Some(m.to)
        case _       => None
      }
    else None
  }

  // makes move even if illegal
  def forceMove(gameMove: GameMove, state: GameState): GameState = {
    gameMove match {
      case m @ CastlingMove(kingMove, rookMove) =>
        state.movePiece(kingMove).movePiece(rookMove).addMoveToHistory(m)

      case m @ Move(piece, from, to) =>
        def enPassantCheck(pieces: Map[Vec2d, Piece]) =
          enPassanedPiecePos(m, state).map(pieces.removed(_)).getOrElse(pieces)

        def movePiece(pieces: Map[Vec2d, Piece]) = pieces
          .removed(from)
          .updated(to, piece)

        def nextPieces = (movePiece _ andThen enPassantCheck)(state.pieces)

        state.updatePieces(nextPieces).addMoveToHistory(m)
    }
  }

  def makeMove(from: Vec2d, to: Vec2d, state: GameState): Option[GameState] =
    for {
      piece <- state.pieces.get(from) // piece must exist
      possibleGameMoves = PossibleMoves.getMoveTiles(from, state)
      gameMove <- possibleGameMoves.get(to)
    } yield (forceMove(gameMove, state))
}
