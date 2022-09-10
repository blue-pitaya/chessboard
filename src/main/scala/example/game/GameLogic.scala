package example.game

import example.GameState
import example.Move
import example.models._

object GameLogic {
  // returns None if move is not en passant, position of piece to kill otherwise
  private def enPassanedPiecePos(
      move: Move,
      state: GameState
  ): Option[Vec2d] = {
    val isEnPassant = move.piece.kind == Pawn &&
      PossibleMoves
        .getEnPassantMoves(move.from, move.piece.color, state)
        .contains(move.to)
    if (isEnPassant) state.lastMove.map(_.to) else None
  }

  // TODO: move and state both has info about piece
  // makes move even if illegal
  def forceMove(move: Move, state: GameState): GameState = {
    def enPassantCheck(pieces: Map[Vec2d, Piece]) =
      enPassanedPiecePos(move, state).map(pieces.removed(_)).getOrElse(pieces)

    def movePiece(pieces: Map[Vec2d, Piece]) = pieces
      .removed(move.from)
      .updated(move.to, move.piece)

    def getNextPieces = (movePiece _ andThen enPassantCheck)(state.pieces)

    state.copy(pieces = getNextPieces, lastMove = Some(move))
  }

  def makeMove(from: Vec2d, to: Vec2d, state: GameState): Option[GameState] =
    for {
      piece <- state.pieces.get(from) // piece must exist
      possibleMoves = PossibleMoves.getMoves(from, piece, state)
      _ <- Option.when(possibleMoves.contains(to))()
      move = Move(piece, from, to)
    } yield (forceMove(move, state))
}
