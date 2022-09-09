package example.game

import example.GameState
import example.models.Vec2d._
import example.models._

object PossibleMoves {
  private def isInsideBoard(boardSize: Vec2d)(pos: Vec2d): Boolean =
    (pos.x >= 0 && pos.y >= 0 && pos.x < boardSize.x && pos.y < boardSize.y)

  private def mirroredVecs(vecs: Seq[Vec2d]): Seq[Vec2d] = vecs ++
    vecs.map(_ * -1)

  private def moveStep(
      pos: Vec2d,
      step: Vec2d,
      stop: Vec2d => Boolean,
      stopBefore: Vec2d => Boolean,
      acc: Set[Vec2d] = Set()
  ): Set[Vec2d] = {
    lazy val currentAcc = acc + pos
    lazy val nextPos = pos + step

    if (stop(pos)) currentAcc
    else if (stopBefore(nextPos)) currentAcc
    else moveStep(nextPos, step, stop, stopBefore, currentAcc)
  }

  private def lineAttacks(
      pos: Vec2d,
      step: Vec2d,
      state: GameState
  ): Set[Vec2d] = {
    val stop = (v: Vec2d) => state.pieces.get(v).isDefined && v != pos
    val stopBefore = (v: Vec2d) => !isInsideBoard(state.size)(v)
    moveStep(pos, step, stop, stopBefore) - pos
  }

  private def knightAttacks(pos: Vec2d, state: GameState): Set[Vec2d] = {
    val steps =
      mirroredVecs(Seq(Vec2d(2, 1), Vec2d(1, 2), Vec2d(-2, 1), Vec2d(-1, 2)))
    val moves = steps.map(_ + pos)
    moves.filter(isInsideBoard(state.size)).toSet
  }

  private def bishopAttacks(pos: Vec2d, state: GameState): Set[Vec2d] = {
    val steps = mirroredVecs(Seq(Vec2d(1, 1), Vec2d(-1, 1)))
    steps.flatMap(s => lineAttacks(pos, s, state)).toSet
  }

  private def rookAttacks(pos: Vec2d, state: GameState): Set[Vec2d] = {
    val steps = mirroredVecs(Seq(Vec2d(0, 1), Vec2d(1, 0)))
    steps.flatMap(s => lineAttacks(pos, s, state)).toSet
  }

  private def queenAttacks(pos: Vec2d, state: GameState): Set[Vec2d] =
    rookAttacks(pos, state) ++ bishopAttacks(pos, state)

  private def pawnAttacks(
      pos: Vec2d,
      color: PieceColor,
      state: GameState
  ): Set[Vec2d] = {
    val steps = color match {
      case Black => Seq(Vec2d(-1, -1), Vec2d(1, -1))
      case White => Seq(Vec2d(-1, 1), Vec2d(1, 1))
    }
    val moves = steps.map(_ + pos)
    moves.filter(isInsideBoard(state.size)).toSet
  }

  private def kingAttacks(pos: Vec2d, state: GameState): Set[Vec2d] = {
    val steps =
      mirroredVecs(Seq(Vec2d(0, 1), Vec2d(1, 1), Vec2d(1, 0), Vec2d(1, -1)))
    val moves = steps.map(_ + pos)
    moves.filter(isInsideBoard(state.size)).toSet
  }

  private def getAttacks(
      pos: Vec2d,
      piece: Piece,
      state: GameState
  ): Set[Vec2d] = piece.kind match {
    case King   => kingAttacks(pos, state)
    case Bishop => bishopAttacks(pos, state)
    case Knight => knightAttacks(pos, state)
    case Queen  => queenAttacks(pos, state)
    case Rook   => rookAttacks(pos, state)
    case Pawn   => pawnAttacks(pos, piece.color, state)
  }

  private def getAllAttacks(color: PieceColor, state: GameState): Set[Vec2d] =
    state
      .pieces
      .filter { case (_, piece) => piece.color == color }
      .flatMap { case (pos, piece) => getAttacks(pos, piece, state) }
      .toSet

  private def movesFromAttacks(
      attacks: Set[Vec2d],
      color: PieceColor,
      state: GameState
  ): Set[Vec2d] = attacks.filter(pos =>
    state.pieces.get(pos).map(p => p.color != color).getOrElse(true)
  )

  private def pawnMoves(
      pos: Vec2d,
      color: PieceColor,
      state: GameState
  ): Set[Vec2d] = {
    // attack
    val attackMoves = pawnAttacks(pos, color, state).filter(p =>
      state.pieces.get(p).map(p => p.color != color).getOrElse(false)
    )

    // 1 up
    val step = if (color == White) Vec2d(0, 1) else Vec2d(0, -1)
    def canMove(v: Vec2d) = isInsideBoard(state.size)(v) &&
      !state.pieces.get(v).isDefined
    val moveOneTile = pos + step
    val regularMoves = if (canMove(moveOneTile)) Seq(moveOneTile) else Seq()

    // 2 up
    val isOnStartingFile = if (color == White) pos.y == 1 else pos.y == 6
    val moveTwoTiles = pos + (step * 2)
    val doubleMoves =
      if (isOnStartingFile && canMove(moveOneTile) && canMove(moveTwoTiles))
        Seq(moveTwoTiles)
      else Seq()

    // en passant
    val enPassantMoves = state
      .lastMove
      .map { move =>
        val lastMoveIsNearPawnDoubleUp = move.piece.kind == Pawn &&
          Math.abs(move.from.y - move.to.y) == 2 &&
          Math.abs(pos.x - move.to.x) == 1

        lazy val attackStep = color match {
          case Black => Vec2d(move.to.x - pos.x, -1)
          case White => Vec2d(move.to.x - pos.x, 1)
        }

        if (lastMoveIsNearPawnDoubleUp) Seq(pos + attackStep) else Seq()
      }
      .getOrElse(Seq())

    (attackMoves ++ regularMoves ++ doubleMoves ++ enPassantMoves).toSet
  }

  // TODO: king is under check
  private def kingMoves(
      pos: Vec2d,
      color: PieceColor,
      state: GameState
  ): Set[Vec2d] = {
    val unrestrictedMoves =
      movesFromAttacks(kingAttacks(pos, state), color, state)
    val attacks = getAllAttacks(color.opposite, state)
    unrestrictedMoves -- attacks
  }

  def getMoves(pos: Vec2d, piece: Piece, state: GameState): Set[Vec2d] =
    piece.kind match {
      case King => kingMoves(pos, piece.color, state)
      case _: MajorPieceType =>
        movesFromAttacks(getAttacks(pos, piece, state), piece.color, state)
      case Pawn => pawnMoves(pos, piece.color, state)
    }

  def getMoves(pos: Vec2d, state: GameState): Set[Vec2d] = state
    .pieces
    .get(pos)
    .map(getMoves(pos, _, state))
    .getOrElse(Set())
}
