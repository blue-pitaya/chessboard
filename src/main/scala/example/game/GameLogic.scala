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

  def makeMove(from: Vec2d, to: Vec2d, state: GameState): Option[GameState] = {
    def enPassantCheck(move: Move)(pieces: Map[Vec2d, Piece]) =
      enPassanedPiecePos(move, state).map(pieces.removed(_)).getOrElse(pieces)

    def movePiece(piece: Piece)(pieces: Map[Vec2d, Piece]) = pieces
      .removed(from)
      .updated(to, piece)

    def getNextPieces(move: Move) =
      (movePiece(move.piece) _ andThen enPassantCheck(move))(state.pieces)

    for {
      piece <- state.pieces.get(from) // piece must exist
      possibleMoves = PossibleMoves.getMoves(from, piece, state)
      _ <- Option.when(possibleMoves.contains(to))()
      move = Move(piece, from, to)
      nextPieces = getNextPieces(move)
    } yield (state.copy(pieces = nextPieces, lastMove = Some(move)))
  }
}
