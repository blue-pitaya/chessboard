package example.game

import example.GameState
import example.models.Vec2d
import example.models.Vec2d._
import example.models.Piece

object PossibleMoves {
  private def isInsideBoard(boardSize: Vec2d)(pos: Vec2d): Boolean =
    (pos.x >= 0 && pos.y >= 0 && pos.x < boardSize.x && pos.y < boardSize.y)

  private def moveStep(
      pos: Vec2d,
      step: Vec2d,
      stop: Vec2d => Boolean,
      stopBefore: Vec2d => Boolean,
      acc: Set[Vec2d] = Set()
  ): Set[Vec2d] =
    if (stop(pos)) acc + pos
    else {
      val nextPos = pos + step
      val stopNow = stopBefore(nextPos)
      if (stopNow) acc + pos
      else moveStep(nextPos, step, stop, stopBefore, acc + pos)
    }

  private def lineMoves(pos: Vec2d, step: Vec2d, state: GameState): Set[Vec2d] =
    (for {
      piece <- state.pieces.get(pos)
      stopBefore = (v: Vec2d) =>
        state.pieces.get(v).map(_.color == piece.color).getOrElse(false) ||
          !isInsideBoard(state.size)(v)
      stop = (v: Vec2d) =>
        state.pieces.get(v).map(_.color != piece.color).getOrElse(false)
      moves = moveStep(pos, step, stop, stopBefore)
    } yield (moves - pos)).getOrElse(Set())

  private def mirroredVecs(vecs: Seq[Vec2d]): Seq[Vec2d] = vecs ++
    vecs.map(_ * -1)

  def knightMoves(piecePos: Vec2d, state: GameState): Set[Vec2d] = {
    (for {
      piece <- state.pieces.get(piecePos)
      steps =
        mirroredVecs(Seq(Vec2d(2, 1), Vec2d(1, 2), Vec2d(-2, 1), Vec2d(-1, 2)))
      positions = steps.map(_ + piecePos)
      moves = positions
        .filter(isInsideBoard(state.size))
        .filter { pos =>
          state.pieces.get(pos).map(p => p.color != piece.color).getOrElse(true)
        }
    } yield (moves.toSet)).getOrElse(Set())
  }

  def bishopMoves(piecePos: Vec2d, state: GameState): Set[Vec2d] = {
    val steps = mirroredVecs(Seq(Vec2d(1, 1), Vec2d(-1, 1)))
    steps.flatMap(s => lineMoves(piecePos, s, state)).toSet
  }

  def rookMoves(piecePos: Vec2d, state: GameState): Set[Vec2d] = {
    val steps = mirroredVecs(Seq(Vec2d(0, 1), Vec2d(1, 0)))
    steps.flatMap(s => lineMoves(piecePos, s, state)).toSet
  }

  def queenMoves(piecePos: Vec2d, state: GameState): Set[Vec2d] =
    rookMoves(piecePos, state) ++ bishopMoves(piecePos, state)

  def pawnMoves(piecePos: Vec2d, state: GameState): Set[Vec2d] = ???

  def kingMoves(piecePos: Vec2d, state: GameState): Set[Vec2d] = ???
}
