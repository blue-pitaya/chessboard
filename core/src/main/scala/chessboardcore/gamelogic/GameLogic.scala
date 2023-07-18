package example.game

import chessboardcore.Vec2d
import chessboardcore.Model._

object GameLogic {
  // returns None if move is not en passant, position of piece to kill otherwise
  private def enPassanedPiecePos(
      move: TrueMove,
      state: TrueGameState
  ): Option[Vec2d] = {
    val isEnPassant = move.piece.kind == Pawn &&
      PossibleMoves
        .getEnPassantMoves(move.from, move.piece.color, state.lastMove)
        .contains(move.to)
    if (isEnPassant) state
      .lastMove
      .flatMap {
        case m: TrueMove => Some(m.to)
        case _           => None
      }
    else None
  }

  // makes move even if illegal
  def forceMove(gameMove: GameMove, state: TrueGameState): TrueGameState = {
    gameMove match {
      case m @ CastlingMove(kingMove, rookMove) =>
        state.movePiece(kingMove).movePiece(rookMove).addMoveToHistory(m)

      case m @ TrueMove(piece, from, to) =>
        def enPassantCheck(pieces: Map[Vec2d, Piece]) =
          enPassanedPiecePos(m, state).map(pieces.removed(_)).getOrElse(pieces)

        def movePiece(pieces: Map[Vec2d, Piece]) = pieces
          .removed(from)
          .updated(to, piece)

        def nextPieces = (movePiece _ andThen enPassantCheck)(state.pieces)

        state.updatePieces(nextPieces).addMoveToHistory(m)
    }
  }

  def makeMove(
      from: Vec2d,
      to: Vec2d,
      state: TrueGameState
  ): Option[TrueGameState] = for {
    piece <- state.pieces.get(from) // piece must exist
    possibleGameMoves = PossibleMoves.getMoveTiles(from, state)
    gameMove <- possibleGameMoves.get(to)
  } yield (forceMove(gameMove, state))
}
