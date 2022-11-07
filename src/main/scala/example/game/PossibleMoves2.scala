package example.game

import example.GameState
import example.models.Vec2d._
import example.models._
import example.Move

object PossibleMoves2 {
  private def isInsideBoard(boardSize: Vec2d)(pos: Vec2d): Boolean =
    (pos.x >= 0 && pos.y >= 0 && pos.x < boardSize.x && pos.y < boardSize.y)

  private def mirroredVecs(vecs: Set[Vec2d]): Set[Vec2d] = vecs ++
    vecs.map(_ * -1)

  // TODO: something wrong ehre
  private def moveStep(
      pos: Vec2d,
      step: Vec2d,
      stop: (Vec2d, Int) => Boolean,
      stopBefore: (Vec2d, Int) => Boolean,
      acc: Set[Vec2d] = Set(),
      stepsMade: Int = 0
  ): Set[Vec2d] = {
    lazy val currentAcc = acc + pos
    lazy val nextPos = pos + step

    if (stop(pos, stepsMade)) currentAcc
    else if (stopBefore(nextPos, stepsMade)) currentAcc
    else moveStep(nextPos, step, stop, stopBefore, currentAcc, stepsMade + 1)
  }

  private def lineAttacks(
      pos: Vec2d,
      step: Vec2d,
      state: GameState
  ): Set[Vec2d] = {
    val stop = (v: Vec2d, _: Int) => state.pieces.get(v).isDefined && v != pos
    val stopBefore = (v: Vec2d, _: Int) => !isInsideBoard(state.size)(v)
    moveStep(pos, step, stop, stopBefore) - pos
  }

  private def jumpAttack(
      pos: Vec2d,
      step: Vec2d,
      state: GameState
  ): Set[Vec2d] = {
    val move = pos + step
    if (isInsideBoard(state.size)(move)) Set(move) else Set()
  }

  private def lineCountedNonaggresiveMove(
      pos: Vec2d,
      step: Vec2d,
      maxSteps: Int,
      state: GameState
  ): Set[Vec2d] = {
    val stop = (_: Vec2d, steps: Int) => steps == maxSteps
    val stopBefore = (v: Vec2d, _: Int) =>
      !isInsideBoard(state.size)(v) || state.pieces.get(v).isDefined
    moveStep(pos, step, stop, stopBefore) - pos
  }

  private val kingSteps =
    mirroredVecs(Set(Vec2d(0, 1), Vec2d(1, 1), Vec2d(1, 0), Vec2d(1, -1)))
  private val bishopSteps = mirroredVecs(Set(Vec2d(1, 1), Vec2d(-1, 1)))
  private val rookSteps = mirroredVecs(Set(Vec2d(0, 1), Vec2d(1, 0)))
  private val queenSteps = rookSteps ++ bishopSteps
  private val knightSteps =
    mirroredVecs(Set(Vec2d(2, 1), Vec2d(1, 2), Vec2d(-2, 1), Vec2d(-1, 2)))
  private val whitePawnSteps = Set(Vec2d(-1, 1), Vec2d(1, 1))
  private val blackPawnSteps = Set(Vec2d(-1, -1), Vec2d(1, -1))

  private def getAttacks(
      pos: Vec2d,
      piece: Piece,
      jumpAttackSteps: Vec2d => Set[Vec2d],
      lineAttackSteps: Vec2d => Set[Vec2d]
  ): Set[Vec2d] = {
    def attacks(steps: Set[Vec2d], extendSteps: Vec2d => Set[Vec2d]) = steps
      .flatMap(extendSteps)
      .toSet

    piece match {
      case Piece(King, _)     => attacks(kingSteps, jumpAttackSteps)
      case Piece(Bishop, _)   => attacks(bishopSteps, lineAttackSteps)
      case Piece(Knight, _)   => attacks(knightSteps, jumpAttackSteps)
      case Piece(Queen, _)    => attacks(queenSteps, lineAttackSteps)
      case Piece(Rook, _)     => attacks(rookSteps, lineAttackSteps)
      case Piece(Pawn, White) => attacks(whitePawnSteps, jumpAttackSteps)
      case Piece(Pawn, Black) => attacks(blackPawnSteps, jumpAttackSteps)
    }
  }

  private def getAttacks(
      pos: Vec2d,
      piece: Piece,
      state: GameState
  ): Set[Vec2d] = {
    def jumpAttackSteps = (step: Vec2d) => jumpAttack(pos, step, state)
    def lineAttackSteps = (step: Vec2d) => lineAttacks(pos, step, state)

    getAttacks(pos, piece, jumpAttackSteps, lineAttackSteps)
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

  def getEnPassantMoves(
      pos: Vec2d,
      color: PieceColor,
      lastMove: Option[Move]
  ): Set[Vec2d] = lastMove
    .map { move =>
      val lastMoveIsNearPawnDoubleUp = move.piece.kind == Pawn &&
        Math.abs(move.from.y - move.to.y) == 2 &&
        Math.abs(pos.x - move.to.x) == 1

      val lastMovedPieceIsOnSameRank = move.to.y == pos.y

      lazy val attackStep = color match {
        case Black => Vec2d(move.to.x - pos.x, -1)
        case White => Vec2d(move.to.x - pos.x, 1)
      }

      if (lastMoveIsNearPawnDoubleUp && lastMovedPieceIsOnSameRank)
        Seq(pos + attackStep)
      else Seq()
    }
    .getOrElse(Seq())
    .toSet

  private def pawnMoves(
      pos: Vec2d,
      color: PieceColor,
      lastMove: Option[Move],
      isOppositeColor: Vec2d => Boolean,
      pawnAttacks: Set[Vec2d],
      stepMoves: (Vec2d) => Set[Vec2d]
  ): Set[Vec2d] = {
    // attack
    val attackMoves = movesFromAttacks(pawnAttacks, isOppositeColor)

    // normal moves
    val step = if (color == White) Vec2d(0, 1) else Vec2d(0, -1)
    val normalMoves = stepMoves(step)

    // en passant
    val enPassantMoves = getEnPassantMoves(pos, color, lastMove)

    (attackMoves ++ normalMoves ++ enPassantMoves).toSet
  }

  private def isKingUnderCheck(color: PieceColor, state: GameState): Boolean =
    state
      .pieces
      .find { case (_, piece) => piece.kind == King && piece.color == color }
      .map { case (pos, king) =>
        val attacks = getAllAttacks(color.opposite, state)
        attacks.contains(pos)
      }
      .getOrElse(false) // king can't be checked if there is no king ¯\_(ツ)_/¯

  private def movesFromAttacks(
      attacks: Set[Vec2d],
      isSameColor: Vec2d => Boolean
  ) = attacks.filter(isSameColor)

  def getMoves(pos: Vec2d, piece: Piece, state: GameState): Set[Vec2d] = {
    val color = piece.color
    def isOppositeColor(pos: Vec2d) = state
      .pieces
      .get(pos)
      .map(_.color != color)
    // TODO: ???
    def isOppositeColorOrEmpty(pos: Vec2d) = isOppositeColor(pos)
      .getOrElse(true)
    def isOppositeColorNotEmpty(pos: Vec2d) = isOppositeColor(pos)
      .getOrElse(false)

    def jumpAttackSteps(step: Vec2d) = jumpAttack(pos, step, state)
    def lineAttackSteps(step: Vec2d) = lineAttacks(pos, step, state)

    val attacks = getAttacks(pos, piece, jumpAttackSteps, lineAttackSteps)
    val moves = piece.kind match {
      case Pawn =>
        val maxSteps = color match {
          case Black if pos.y == 6 => 2
          case White if pos.y == 1 => 2
          case _                   => 1
        }
        def pawnMoveSteps(step: Vec2d) =
          lineCountedNonaggresiveMove(pos, step, maxSteps, state)
        pawnMoves(
          pos,
          piece.color,
          state.lastMove,
          isOppositeColorNotEmpty,
          attacks,
          pawnMoveSteps
        )
      case _ => movesFromAttacks(attacks, isOppositeColorOrEmpty)
    }

    def simulatedMove(to: Vec2d) = GameLogic
      .forceMove(Move(piece, pos, to), state)

    moves.filter(to => !isKingUnderCheck(color, simulatedMove(to)))
  }

  def getMoves(pos: Vec2d, state: GameState): Set[Vec2d] = state
    .pieces
    .get(pos)
    .map(getMoves(pos, _, state))
    .getOrElse(Set())
}
